
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
     * ������ �����û��������¼�(���button, ������Ļ....)�������̸߳�����ģ�������̴߳��ڹ���״̬��
     * ��ʱ�û������������¼����û����5���ڵõ�����ϵͳ�ͻᱨ��Ӧ������Ӧ������
     * ���������߳��ﲻ��ִ��һ���ȽϺ�ʱ�Ĺ���������������߳��������޷������û��������¼���
     * ���¡�Ӧ������Ӧ������ĳ��֡���ʱ�Ĺ���Ӧ�������߳���ִ�С�
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
     * ��ͣ��Ӧ�����������������û���˾͵���stopSelf
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
     * UI�ؼ�������ػ�(����)�������̸߳�����ģ���������߳��и���UI�ؼ���ֵ�����º��ֵ�����ػ浽��Ļ��
     * һ��Ҫ�����߳������UI�ؼ���ֵ��������������Ļ����ʾ���������������߳��и���UI�ؼ���ֵ
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
         * �˳�����
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
                    // �����ļ����ֵ������֣������������ֵ�ͻ���100
                    sendMaxMsg(loader.getFileSize());

                    // ���½�����
                    sendProgressMsg(size);
                }
                if (size == loader.getFileSize()) {// �������
                    mTasks.remove(path);
                }
            }

            @Override
            public void onPause(String path) {
                // ������ͣ��Ϣ
                sendPauseMsg();
            }

            @Override
            public void onFinish(String path) {

            }
        };

        @Override
        public void run() {
            try {
                // ʵ����һ���ļ�������
                isRunning = true;
                loader = new FileDownloader(getApplicationContext(), path,
                        saveDir, 3);
                loader.getDownloadSize();
                print("to setmax : handler=" + mHandler);
                if (mHandler != null) {
                    // �������ý��������ֵ
                    sendMaxMsg(loader.getFileSize());
                    // ���͵�ǰ������
                    sendProgressMsg(loader.getDownloadSize());
                }
                // ��ʼ����
                loader.download(downloadProgressListener);
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.sendMessage(mHandler.obtainMessage(FAILURE)); // ����һ������Ϣ����
            }
            isRunning = false;
            if (mTasks.containsKey(path)) {
                mTasks.remove(path);
                if (mTasks.size() <= 0) {
                    DownloadService.this.stopSelf();
                }
            }
        }

        // ���½��������ֵ
        private void sendMaxMsg(int size) {
            if (mHandler != null) {
                // �����ļ����ֵ������֣������������ֵ�ͻ���100
                Message msg1 = new Message();
                msg1.what = MSG_BAR_MAX;
                msg1.arg1 = size;
                msg1.getData().putString(PATH, path);
                mHandler.sendMessage(msg1);
            }
        }

        // ���½�����
        private void sendProgressMsg(int size) {
            if (mHandler != null) {
                Message msg = new Message();
                msg.getData().putString(PATH, path);
                msg.what = PROCESSING;
                msg.getData().putInt(SIZE, size);
                mHandler.sendMessage(msg);
            }
        }

        // ֪ͨ��ͣ
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
