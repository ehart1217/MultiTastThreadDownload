
package com.wwj.download;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;
import android.widget.Toast;

import com.wwj.download.adapter.DownloadListAdapter;
import com.wwj.download.adapter.DownloadListAdapter.UrlBean;
import com.wwj.download.view.CircleProgressBar;
import com.wwj.download.view.CircleProgressBar.BtnStatus;
import com.wwj.net.download.DownloadService;

public class MainActivity extends Activity {
    private static final int PROCESSING = 1;
    private static final int FAILURE = -1;

    private Handler mHandler = new UIHandler();

    private DownloadListAdapter mAdapter;

    private Context mContext;

    private final class UIHandler extends Handler {
        public void handleMessage(Message msg) {
            String path = msg.getData().getString(DownloadService.PATH);
            switch (msg.what) {
            // 更新进度
                case PROCESSING:
                    // print("handler msg path:" + path);
                    updateListView(path, msg.getData().getInt("size"));
                    break;
                // 下载失败
                case FAILURE:
                    Toast.makeText(getApplicationContext(), R.string.error,
                            Toast.LENGTH_LONG).show();
                    updateListViewPause(path);// 更新UI,切换开关
                    // 暂停
                case DownloadService.PAUSE:
                    updateListViewPause(path);// 更新UI,切换开关
                    break;
                case DownloadService.MSG_BAR_MAX:
                    int max = msg.arg1;
                    print("handle msg setmax:" + max);
                    String path_max = (String) msg.getData().get(DownloadService.PATH);
                    mAdapter.getProgressBar(path_max).setMax(max);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.main);
        List<UrlBean> beans = new ArrayList<UrlBean>();
        UrlBean bean1 = new UrlBean("http://abv.cn/music/光辉岁月.mp3", "光辉岁月", "黄家驹唱的", "热度：很热",
                "4Mb",
                getResources().getDrawable(R.drawable.my_icon));
        UrlBean bean2 = new UrlBean(
                "http://sc.111ttt.com/up/mp3/304296/937161E63A1D57484158C7464D7B50B7.mp3",
                "1231234124",
                "test", "热度：很热",
                "4Mb",
                getResources().getDrawable(R.drawable.my_icon));
        UrlBean bean3 = new UrlBean("http://qzone.haoduoge.com/music5/2015-04-19/1429440483.mp3",
                "test", "一个不支持断点续传的链接", "热度：不热",
                "4Mb",
                getResources().getDrawable(R.drawable.my_icon));
        UrlBean bean4 = new UrlBean("http://qzone.haoduoge.com/music5/2015-04-19/1429440637.mp3",
                "test", "test", "热度：很热",
                "4Mb",
                getResources().getDrawable(R.drawable.my_icon));
        beans.add(bean1);
        beans.add(bean2);
        beans.add(bean3);
        beans.add(bean4);

        ListView listView = (ListView) this.findViewById(R.id.download_listview);
        mAdapter = new DownloadListAdapter(mContext, beans, mHandler);
        listView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        mAdapter.unbindDownloadService();
        super.onDestroy();
    }

    /**
     * 更新进度
     * 
     * @param path
     * @param size
     */
    private void updateListView(String path, int size) {

        CircleProgressBar progressBar = mAdapter.getProgressBar(path);
        progressBar.setProgress(size);
        print("handler msg size:" + size);
        print("handler msg size max" + progressBar.getMax());
        // float num = (float) progressBar.getProgress()
        // / (float) progressBar.getMax();
        // int result = (int) (num * 100); // 计算进度

        if (progressBar.getProgress() == progressBar.getMax()) {
            Toast.makeText(getApplicationContext(), R.string.success,
                    Toast.LENGTH_LONG).show();
            // 下载完成，把按钮改成已下载
            btnChange(path, BtnStatus.done);

        } else {
            // 未下载完成，正在下载,把按钮改成正在下载
            btnChange(path, BtnStatus.downloading);
        }
        // mAdapter.notifyDataSetChanged();
    }

    // 暂停的反应
    private void updateListViewPause(String path) {
        btnChange(path, BtnStatus.pause);
    }

    /**
     * 切换暂停和开始按钮
     * 
     * @param path
     * @param b start设置为b；pause设置为!b
     */
    private void btnChange(String path, BtnStatus btnStatus) {
        mAdapter.getViewHolder(path).progressBar.setStatus(btnStatus);
    }

    private void print(String str) {
        DownloadListAdapter.print(str);
    }

}
