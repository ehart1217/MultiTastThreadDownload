
package com.wwj.download;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.wwj.download.adapter.DownloadListAdapter;
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
                case PROCESSING: // 更新进度
                    // print("handler msg path:" + path);

                    updateListView(path, msg.getData().getInt("size"));
                    break;
                case FAILURE: // 下载失败
                    Toast.makeText(getApplicationContext(), R.string.error,
                            Toast.LENGTH_LONG).show();
                    updateListViewPause(path);// 更新UI,切换开关
                case DownloadService.PAUSE: // 暂停
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
        List<String> paths = new ArrayList<String>();
        paths.add("http://abv.cn/music/list.php");
        paths.add("http://abv.cn/music/光辉岁月.mp3");
        paths.add("http://sc.111ttt.com/up/mp3/304296/937161E63A1D57484158C7464D7B50B7.mp3");
        paths.add("http://qzone.haoduoge.com/music5/2015-04-19/1429440483.mp3");
        paths.add("http://qzone.haoduoge.com/music5/2015-04-19/1429436647.mp3");
        paths.add("http://qzone.haoduoge.com/music5/2015-04-19/1429440637.mp3");

        ListView listView = (ListView) this.findViewById(R.id.download_listview);
        mAdapter = new DownloadListAdapter(mContext, paths, mHandler);
        listView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        mAdapter.unbindDownloadService();
        super.onDestroy();
    }

    /**
     * @param path
     * @param size
     */
    private void updateListView(String path, int size) {

        ProgressBar progressBar = mAdapter.getProgressBar(path);
        progressBar.setProgress(size);
        print("handler msg size:" + size);
        print("handler msg size max" + progressBar.getMax());
        float num = (float) progressBar.getProgress()
                / (float) progressBar.getMax();
        int result = (int) (num * 100); // 计算进度
        mAdapter.getResultView(path).setText(result + "%");

        if (progressBar.getProgress() == progressBar.getMax()) {
            Toast.makeText(getApplicationContext(), R.string.success,
                    Toast.LENGTH_LONG).show();
            btnChange(path, true);

        } else {
            btnChange(path, false);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void updateListViewPause(String path) {
        btnChange(path, true);
    }

    /**
     * 切换暂停和开始按钮
     * 
     * @param path
     * @param b start设置为b；pause设置为!b
     */
    private void btnChange(String path, boolean b) {
        mAdapter.getViewHolder(path).downloadBtn.setEnabled(b);
        mAdapter.getViewHolder(path).pauseBtn.setEnabled(!b);
    }

    private void print(String str) {
        DownloadListAdapter.print(str);
    }

}
