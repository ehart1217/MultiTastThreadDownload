
package com.wwj.net.download;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class DownloadService extends Service {

    public static final int PROCESSING = 1;
    public static final int FAILURE = -1;
    public static final int MSG_BAR_MAX = 2;
    
    public static final String SIZE = "size";
    public static final String PATH = "path";

    @Override
    public IBinder onBind(Intent intent) {
        return new DlBinder();
    }

    public class DlBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    /*
     * 由于用户的输入事件(点击button, 触摸屏幕....)是由主线程负责处理的，如果主线程处于工作状态，
     * 此时用户产生的输入事件如果没能在5秒内得到处理，系统就会报“应用无响应”错误。
     * 所以在主线程里不能执行一件比较耗时的工作，否则会因主线程阻塞而无法处理用户的输入事件，
     * 导致“应用无响应”错误的出现。耗时的工作应该在子线程里执行。
     */
    private DownloadTask task;

    private Handler mHandler;

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void exit() {
        if (task != null)
            task.exit();
    }

    public void download(String path, File savDir) {
        task = new DownloadTask(path, savDir);
        new Thread(task).start();
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

        /**
         * 退出下载
         */
        public void exit() {
            if (loader != null)
                loader.exit();
        }

        DownloadProgressListener downloadProgressListener = new DownloadProgressListener() {
            @Override
            public void onDownloadSize(int size) {
                Message msg = new Message();
                msg.put(PATH,);
                msg.what = PROCESSING;
                msg.getData().putInt(SIZW, size);
                mHandler.sendMessage(msg);

            }
        };

        @Override
        public void run() {
            try {
                // 实例化一个文件下载器
                loader = new FileDownloader(getApplicationContext(), path,
                        saveDir, 3);
                // 发送设置进度条最大值
                Message msg = new Message();
                msg.what = MSG_BAR_MAX;
                msg.arg1 = loader.getFileSize();
                mHandler.sendMessage(msg);

                // 开始下载
                loader.download(downloadProgressListener);
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.sendMessage(mHandler.obtainMessage(FAILURE)); // 发送一条空消息对象
            }
        }
    }

}
