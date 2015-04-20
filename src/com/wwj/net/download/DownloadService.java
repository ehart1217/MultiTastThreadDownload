
package com.wwj.net.download;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.wwj.download.adapter.DownloadListAdapter;

public class DownloadService extends Service {

    public static final String TAG = "DownloadService";

    public static final int PROCESSING = 1;
    public static final int FAILURE = -1;
    public static final int MSG_BAR_MAX = 2;
    public static final int PAUSE = 3;

    public static final String SIZE = "size";
    public static final String PATH = "path";

    public class DlBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return new DlBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    /*
     * 多任务； 由于用户的输入事件(点击button, 触摸屏幕....)是由主线程负责处理的，如果主线程处于工作状态，
     * 此时用户产生的输入事件如果没能在5秒内得到处理，系统就会报“应用无响应”错误。
     * 所以在主线程里不能执行一件比较耗时的工作，否则会因主线程阻塞而无法处理用户的输入事件，
     * 导致“应用无响应”错误的出现。耗时的工作应该在子线程里执行。
     */
    private Map<String, DownloadTask> mTasks;

    // private DownloadTask task;

    private Handler mHandler;

    public DownloadService() {
        mTasks = new HashMap<String, DownloadTask>();
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    /**
     * 暂停相应的下载任务，如果任务没有了就调用stopSelf
     * 
     * @param path
     */
    public void exit(String path) {
        DownloadTask task = mTasks.get(path);
        if (task != null) {
            task.exit();
            mTasks.remove(path);
            if (mTasks.size() <= 0) {
                this.stopSelf();
            }
        }
    }

    public void download(String path, File savDir) {
        if ((mTasks != null && !mTasks.containsKey(path))
                || (mTasks != null && !mTasks.get(path).isRunning)) {
            DownloadTask task = new DownloadTask(path, savDir);
            new Thread(task).start();
            mTasks.put(path, task);
        }
    }

    /**
     * UI控件画面的重绘(更新)是由主线程负责处理的，如果在子线程中更新UI控件的值，更新后的值不会重绘到屏幕上
     * 一定要在主线程里更新UI控件的值，这样才能在屏幕上显示出来，不能在子线程中更新UI控件的值
     */
    private final class DownloadTask implements Runnable {
        private String path;
        private File saveDir;
        private FileDownloader loader;

        public DownloadTask(String path, File saveDir) {
            this.path = path;
            this.saveDir = saveDir;
        }

        private boolean isRunning = false;

        /**
         * 退出下载
         */
        public void exit() {
            if (loader != null)
                loader.exit();
        }

        public boolean isRunning() {
            return isRunning;
        }

        DownloadProgressListener downloadProgressListener = new DownloadProgressListener() {
            @Override
            public void onDownloadingSize(String path, int size) {
                if (mHandler != null) {
                    // 发送文件最大值，好奇怪，不反复发最大值就会变成100
                    sendMaxMsg(loader.getFileSize());

                    // 更新进度条
                    sendProgressMsg(size);
                }
                if (size == loader.getFileSize()) {// 下载完成
                    mTasks.remove(path);
                }
            }

            @Override
            public void onPause(String path) {
                // 发送暂停消息
                sendPauseMsg();
            }

            @Override
            public void onFinish(String path) {

            }
        };

        @Override
        public void run() {
            try {
                // 实例化一个文件下载器
                isRunning = true;
                loader = new FileDownloader(getApplicationContext(), path,
                        saveDir, 3);
                loader.getDownloadSize();
                print("to setmax : handler=" + mHandler);
                if (mHandler != null) {
                    // 发送设置进度条最大值
                    sendMaxMsg(loader.getFileSize());
                    // 发送当前已下载
                    sendProgressMsg(loader.getDownloadSize());
                }
                // 开始下载
                loader.download(downloadProgressListener);
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.sendMessage(mHandler.obtainMessage(FAILURE)); // 发送一条空消息对象
            }
            isRunning = false;
            if (mTasks.containsKey(path)) {
                mTasks.remove(path);
                if (mTasks.size() <= 0) {
                    DownloadService.this.stopSelf();
                }
            }
        }

        // 更新进度条最大值
        private void sendMaxMsg(int size) {
            if (mHandler != null) {
                // 发送文件最大值，好奇怪，不反复发最大值就会变成100
                Message msg1 = new Message();
                msg1.what = MSG_BAR_MAX;
                msg1.arg1 = size;
                msg1.getData().putString(PATH, path);
                mHandler.sendMessage(msg1);
            }
        }

        // 更新进度条
        private void sendProgressMsg(int size) {
            if (mHandler != null) {
                Message msg = new Message();
                msg.getData().putString(PATH, path);
                msg.what = PROCESSING;
                msg.getData().putInt(SIZE, size);
                mHandler.sendMessage(msg);
            }
        }

        // 通知暂停
        private void sendPauseMsg() {
            Message msg = new Message();
            msg.getData().putString(PATH, path);
            msg.what = PAUSE;
            mHandler.sendMessage(msg);
        }
    }

    private void print(String str) {
        DownloadListAdapter.print(str);
    }
}
