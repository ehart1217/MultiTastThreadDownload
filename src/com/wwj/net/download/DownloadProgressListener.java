
package com.wwj.net.download;

public interface DownloadProgressListener {
    /**
     * @param path �����ļ���Զ��·��
     * @param size ��ǰ������
     */
    public void onDownloadingSize(String path, int size);

    public void onPause(String path);

    public void onFinish(String path);
}
