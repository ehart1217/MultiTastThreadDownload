
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

    public void exit(String path) {
        DownloadTask task = mTasks.get(path);
        if (task != null)
            task.exit();
    }

    public void download(String path, File savDir) {
        DownloadTask task = new DownloadTask(path, savDir);
        new Thread(task).start();
        mTasks.put(path, task);
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

        /**
         * �˳�����
         */
        public void exit() {
            if (loader != null)
                loader.exit();
        }

        DownloadProgressListener downloadProgressListener = new DownloadProgressListener() {
            @Override
            public void onDownloadSize(String path, int size) {
                Message msg = new Message();
                msg.getData().putString(PATH, path);
                msg.what = PROCESSING;
                msg.getData().putInt(SIZE, size);
                mHandler.sendMessage(msg);

            }
        };

        @Override
        public void run() {
            try {
                // ʵ����һ���ļ�������
                loader = new FileDownloader(getApplicationContext(), path,
                        saveDir, 3);
                // �������ý��������ֵ
                Message msg = new Message();
                msg.what = MSG_BAR_MAX;
                msg.arg1 = loader.getFileSize();
                msg.getData().putString(PATH, path);
                mHandler.sendMessage(msg);

                // ��ʼ����
                loader.download(downloadProgressListener);
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.sendMessage(mHandler.obtainMessage(FAILURE)); // ����һ������Ϣ����
            }
        }
    }

}
