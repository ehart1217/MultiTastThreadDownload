
package com.wwj.download.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wwj.download.R;
import com.wwj.download.util.CommonTools;
import com.wwj.net.download.DownloadService;

/**
 * 下载列表适配器
 * 
 * @author WanChi
 */
public class DownloadListAdapter extends BaseAdapter {
    public static final String DOWNLOAD = "download";

    public static class ViewHolder {
        public EditText editView;
        public Button downloadBtn;
        public Button pauseBtn;
        public ProgressBar progressBar;
        public TextView resultView;
    }

    private List<String> mPaths;
    private List<String> mEncodedPaths;
    private Context mContext;
    private DownloadService mService;
    private Map<String, ViewHolder> mHolders;
    /**
     * 处理service发送过来的消息
     */
    private Handler mHandler;

    /**
     * 创建一个适配器，绑定一个下载服务（不调用startService，目的只是拿到service对象）。
     * 当点击下载的时候才会调用startService（目的是建立一个与activity无关的service），所有的任务下载完成的时候会
     * 
     * @param context
     * @param paths 下载路径列表
     * @param handler 通知更新UI
     */
    public DownloadListAdapter(Context context, List<String> paths, Handler handler) {
        mPaths = paths;
        mContext = context;
        mHandler = handler;
        mHolders = new HashMap<String, ViewHolder>();
        mEncodedPaths = new ArrayList<String>();
        for (String path : paths) {
            String encodedPath = encodePath(path);
            mEncodedPaths.add(encodedPath);
        }

        bindDownloadService();// 绑定服务，可以和activity交互
    }

    @Override
    public int getCount() {
        return mEncodedPaths.size();
    }

    @Override
    public String getItem(int position) {
        return mEncodedPaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.download_item, parent,
                    false);
            viewHolder = new ViewHolder();
            viewHolder.editView = (EditText) convertView.findViewById(R.id.path);
            viewHolder.downloadBtn = (Button) convertView.findViewById(R.id.downloadbutton);
            viewHolder.pauseBtn = (Button) convertView.findViewById(R.id.stopbutton);
            viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            viewHolder.resultView = (TextView) convertView.findViewById(R.id.resultView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.editView.setText(mPaths.get(position));
        viewHolder.downloadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clickDownloadBtn(mEncodedPaths.get(position));
            }
        });
        viewHolder.pauseBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                clickPauseBtn(mEncodedPaths.get(position));
            }
        });

        mHolders.put(mEncodedPaths.get(position), viewHolder);

        return convertView;
    }

    // 点击下载的时候的处理
    private void clickDownloadBtn(String path) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            // 保存路径
            File savDir = Environment.getExternalStorageDirectory();// 根目录
            if (mService != null) {

                // ViewHolder vh = mHolders.get(path);
                // if (vh != null) {
                // vh.pauseBtn.setEnabled(true);
                // vh.downloadBtn.setEnabled(false);
                // }

                startDownloadService();
                mService.download(path, savDir);

            } else {
                Toast.makeText(mContext,
                        "wait for service starting", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            Toast.makeText(mContext,
                    R.string.sdcarderror, Toast.LENGTH_LONG).show();
        }
    }

    // 点击暂停的处理
    private void clickPauseBtn(String path) {
        if (mService != null) {
            mService.exit(path);
            Toast.makeText(mContext,
                    "Now thread is Stopping!!", Toast.LENGTH_LONG).show();
            // ViewHolder vh = mHolders.get(path);
            // if (vh != null) {
            // vh.pauseBtn.setEnabled(false);
            // vh.downloadBtn.setEnabled(true);
            // }
        } else {
            Toast.makeText(mContext,
                    "wait for service starting", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * 得到下载进度条
     * 
     * @param path
     * @return
     */
    public ProgressBar getProgressBar(String path) {
        return mHolders.get(path).progressBar;
    }

    /**
     * 得到下载百分比显示
     * 
     * @param path
     * @return
     */
    public TextView getResultView(String path) {
        return mHolders.get(path).resultView;
    }

    public ViewHolder getViewHolder(String path) {
        return mHolders.get(path);
    }

    /**
     * 对中文路径进行转码
     * 
     * @param path
     * @return
     */
    private String encodePath(String path) {
        return CommonTools.getEncodePath(path);
    }

    /**
     * 服务连接器
     */
    ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((DownloadService.DlBinder) service).getService();
            mService.setHandler(mHandler);
        }
    };

    public void startDownloadService() {
        Intent intent = new Intent(mContext, DownloadService.class);
        mContext.startService(intent);
    }

    public void bindDownloadService() {
        Intent bindIntent = new Intent(mContext, DownloadService.class);
        mContext.bindService(bindIntent, conn, Context.BIND_AUTO_CREATE);
    }

    public void unbindDownloadService() {
        mContext.unbindService(conn);
    }

    public void stopDownloadService() {
        if (mService != null) {
            mService.stopSelf();
        }
    }

    public static void print(String str) {
        Log.i(DOWNLOAD, str);
    }
}
