
package com.wwj.download.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Environment;
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

public class DownloadListAdapter extends BaseAdapter {

    public static class ViewHolder {
        EditText editView;
        Button downloadBtn;
        Button pauseBtn;
        ProgressBar progressBar;
        TextView resultView;
    }

    private List<String> mPaths;
    private List<String> mEncodedPaths;
    private Context mContext;
    private DownloadService mService;
    private Map<String, ViewHolder> mHolders;

    public DownloadListAdapter(Context context, List<String> paths) {
        mPaths = paths;
        mContext = context;
        mHolders = new HashMap<String, ViewHolder>();
        mEncodedPaths = new ArrayList<String>();
        for (String path : paths) {
            String encodedPath = encodePath(path);
            mEncodedPaths.add(encodedPath);
        }
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
            mHolders.put(mEncodedPaths.get(position), viewHolder);
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
        return convertView;
    }

    private void clickDownloadBtn(String path) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            // 保存路径
            File savDir = Environment.getExternalStorageDirectory();// 根目录
            if (mService != null) {
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
        ViewHolder vh = mHolders.get(path);
        if (vh != null) {
            vh.downloadBtn.setEnabled(false);
            vh.pauseBtn.setEnabled(true);
        }
    }

    private void clickPauseBtn(String path) {
        if (mService != null) {
            mService.exit(path);
            Toast.makeText(mContext,
                    "Now thread is Stopping!!", Toast.LENGTH_LONG).show();
            ViewHolder vh = mHolders.get(path);
            if (vh != null) {
                vh.downloadBtn.setEnabled(false);
                vh.pauseBtn.setEnabled(true);
            }
        } else {
            Toast.makeText(mContext,
                    "wait for service starting", Toast.LENGTH_LONG).show();
        }

    }

    public void setService(DownloadService service) {
        mService = service;
    }

    public ProgressBar getProgressBar(String path) {
        return mHolders.get(path).progressBar;
    }

    public TextView getResultView(String path) {
        return mHolders.get(path).resultView;
    }

    private String encodePath(String path) {
        return CommonTools.getEncodePath(path);
    }
}
