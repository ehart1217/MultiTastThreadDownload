
package com.wwj.download;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wwj.net.download.DownloadService;

public class MainActivity extends Activity {
    private static final int PROCESSING = 1;
    private static final int FAILURE = -1;

    private EditText pathText; // url地址
    private TextView resultView;
    private Button downloadButton;
    private Button stopButton;
    private ProgressBar progressBar;

    private Handler mHandler = new UIHandler();

    private DownloadService mDlService;

    private Context mContext;

    private final class UIHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROCESSING: // 更新进度
                    updateProgressBar(msg.getData().getInt("size"));
                    break;
                case FAILURE: // 下载失败
                    Toast.makeText(getApplicationContext(), R.string.error,
                            Toast.LENGTH_LONG).show();
                    break;
                case DownloadService.MSG_BAR_MAX:
                    int max = msg.arg1;
                    progressBar.setMax(max);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.main);
        pathText = (EditText) findViewById(R.id.path);
        resultView = (TextView) findViewById(R.id.resultView);
        downloadButton = (Button) findViewById(R.id.downloadbutton);
        stopButton = (Button) findViewById(R.id.stopbutton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ButtonClickListener listener = new ButtonClickListener();
        downloadButton.setOnClickListener(listener);
        stopButton.setOnClickListener(listener);
        bindDownloadService();
    }

    private final class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.downloadbutton: // 开始下载
                    // http://abv.cn/music/光辉岁月.mp3，可以换成其他文件下载的链接
                    String path = pathText.getText().toString();
                    String filename = path.substring(path.lastIndexOf('/') + 1);

                    try {
                        // URL编码（这里是为了将中文进行URL编码）
                        filename = URLEncoder.encode(filename, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    // 转码后的路径
                    path = path.substring(0, path.lastIndexOf("/") + 1) + filename;
                    if (Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        // File savDir =
                        // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                        // 保存路径
                        File savDir = Environment.getExternalStorageDirectory();// 根目录
                        if (mDlService != null) {
                            mDlService.download(path, savDir);
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "wait for service starting", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(),
                                R.string.sdcarderror, Toast.LENGTH_LONG).show();
                    }
                    downloadButton.setEnabled(false);
                    stopButton.setEnabled(true);
                    break;
                case R.id.stopbutton: // 暂停下载
                    if (mDlService != null) {
                        mDlService.exit();
                        Toast.makeText(getApplicationContext(),
                                "Now thread is Stopping!!", Toast.LENGTH_LONG).show();
                        downloadButton.setEnabled(true);
                        stopButton.setEnabled(false);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "wait for service starting", Toast.LENGTH_LONG).show();
                    }

                    break;
            }
        }

    }

    private void updateProgressBar(int size) {
        progressBar.setProgress(size);
        float num = (float) progressBar.getProgress()
                / (float) progressBar.getMax();
        int result = (int) (num * 100); // 计算进度
        resultView.setText(result + "%");
        if (progressBar.getProgress() == progressBar.getMax()) {
            Toast.makeText(getApplicationContext(), R.string.success,
                    Toast.LENGTH_LONG).show();
        }
    }

    ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDlService = ((DownloadService.DlBinder) service).getService();
            mDlService.setHandler(mHandler);
        }
    };

    private void bindDownloadService() {
        Intent bindIntent = new Intent(mContext, DownloadService.class);
        bindService(bindIntent, conn, BIND_AUTO_CREATE);
    }

}
