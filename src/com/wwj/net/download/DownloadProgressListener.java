
package com.wwj.net.download;

public interface DownloadProgressListener {
    /**
     * @param path 下载文件的远程路径
     * @param size 当前已下载
     */
    public void onDownloadingSize(String path, int size);

    public void onPause(String path);

    public void onFinish(String path);
}
