
package com.wwj.net.download;

public interface DownloadProgressListener {
    /**
     * @param path �����ļ���Զ��·��
     * @param size ��ǰ������
     */
    public void onDownloadSize(String path, int size);
}
