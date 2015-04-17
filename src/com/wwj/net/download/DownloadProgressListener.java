
package com.wwj.net.download;

public interface DownloadProgressListener {
    /**
     * @param path 下载文件的远程路径
     * @param size 当前已下载
     */
    public void onDownloadSize(String path, int size);
}
