
package com.wwj.download.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wwj.download.R;
import com.wwj.download.util.CommonTools;
import com.wwj.download.view.CircleProgressBar;
import com.wwj.download.view.CircleProgressBar.BtnStatus;
import com.wwj.net.download.DownloadService;
import com.wwj.net.download.FileService;

/**
 * �����б�������
 * 
 * @author WanChi
 */
public class DownloadListAdapter extends BaseAdapter {
    public static final String DOWNLOAD = "download";

    public static class ViewHolder {
        // app_icon_imageView app_name_textView app_heat_textView
        // app_size_textView app_content_textView app_download_button

        public ImageView ivIcon;
        public TextView tvName;
        public TextView tvHeat;
        public TextView tvSize;
        public TextView tvContent;
        public CircleProgressBar progressBar;
        // public EditText editView;
        // public Button downloadBtn;
        // public Button pauseBtn;
        // public CircleProgressBar progressBar;
        // public TextView resultView;
    }

    private List<UrlBean> mBeans;
    private List<String> mEncodedPaths;
    private Context mContext;
    private DownloadService mService;
    private Map<String, ViewHolder> mHolders;
    private List<View> mConvertViews;
    /**
     * ����service���͹�������Ϣ
     */
    private Handler mHandler;

    /**
     * ����һ������������һ�����ط��񣨲�����startService��Ŀ��ֻ���õ�service���󣩡�
     * ��������ص�ʱ��Ż����startService��Ŀ���ǽ���һ����activity�޹ص�service�������е�����������ɵ�ʱ���
     * 
     * @param context
     * @param paths ����·���б�
     * @param handler ֪ͨ����UI
     */
    public DownloadListAdapter(Context context, List<UrlBean> beans, Handler handler) {
        mBeans = beans;
        mContext = context;
        mHandler = handler;
        mHolders = new HashMap<String, ViewHolder>();
        mConvertViews = new ArrayList<View>();
        mEncodedPaths = new ArrayList<String>();
        for (UrlBean bean : mBeans) {
            String encodedPath = encodePath(bean.url);
            mEncodedPaths.add(encodedPath);
        }
        createViews(mEncodedPaths);
        bindDownloadService();// �󶨷��񣬿��Ժ�activity����
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

    @SuppressLint("NewApi")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        String path = mEncodedPaths.get(position);// �����ת��·��
        // ���item�����ݼ���
        UrlBean bean = mBeans.get(position);
        ViewHolder viewHolder = (ViewHolder) mConvertViews.get(position).getTag();
        // ���������ذٷֱ�
        // float num = (float) viewHolder.progressBar.getProgress()
        // / (float) viewHolder.progressBar.getMax();
        // int result = (int) (num * 100); // �������

        viewHolder.tvName.setText(bean.name);
        viewHolder.tvContent.setText(bean.content);
        viewHolder.tvHeat.setText(bean.heat);
        viewHolder.tvSize.setText(bean.size);
        viewHolder.ivIcon.setBackground(bean.icon);

        final CircleProgressBar progressBar = viewHolder.progressBar;
        progressBar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (progressBar.getStatus()) {
                    case ready:
                        clickDownloadBtn(mEncodedPaths.get(position));
                        break;
                    case downloading:
                        clickPauseBtn(mEncodedPaths.get(position));
                        break;
                    case pause:
                        clickDownloadBtn(mEncodedPaths.get(position));
                        break;
                    case done:
                        // TODO ������ɵ����װ
                        break;
                    default:
                        break;
                }
            }
        });

        return mConvertViews.get(position);
    }

    void createViews(List<String> paths) {
        for (String path : paths) {
            View convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.app_item_layout, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.app_name_textView);
            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.app_content_textView);
            viewHolder.tvHeat = (TextView) convertView.findViewById(R.id.app_heat_textView);
            viewHolder.progressBar = (CircleProgressBar) convertView
                    .findViewById(R.id.app_download_button);
            viewHolder.tvSize = (TextView) convertView.findViewById(R.id.app_size_textView);
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.app_icon_imageView);
            convertView.setTag(viewHolder);
            mConvertViews.add(convertView);
            mHolders.put(path, viewHolder);

            // ��ȡ���ݿ��������صĴ�С
            viewHolder.progressBar.setMax(getFileSize(path));
            viewHolder.progressBar.setProgress(getDownloadSize(path));
            // �Ѿ����ع��� ����Ϊ��ͣ״̬
            if (getDownloadSize(path) > 0 && getDownloadSize(path) < getFileSize(path)) {
                viewHolder.progressBar.setStatus(BtnStatus.pause);
            }
            // ��û��ʼ��������Ϊready״̬
            else if (getDownloadSize(path) == 0) {
                viewHolder.progressBar.setStatus(BtnStatus.ready);
            } else if (getDownloadSize(path) == getFileSize(path)) {
                viewHolder.progressBar.setStatus(BtnStatus.done);
            }
        }
    }

    // ������ص�ʱ��Ĵ���
    private void clickDownloadBtn(String path) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            // ����·��
            File savDir = Environment.getExternalStorageDirectory();// ��Ŀ¼
            if (mService != null) {
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

    // �����ͣ�Ĵ���
    private void clickPauseBtn(String path) {
        if (mService != null) {
            mService.exit(path);
            Toast.makeText(mContext,
                    "Now thread is Stopping!!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext,
                    "wait for service starting", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * �õ����ؽ�����
     * 
     * @param path
     * @return
     */
    public CircleProgressBar getProgressBar(String path) {
        return mHolders.get(path).progressBar;
    }

    // /**
    // * �õ����ذٷֱ���ʾ
    // *
    // * @param path
    // * @return
    // */
    // public TextView getResultView(String path) {
    // return mHolders.get(path).resultView;
    // }

    public ViewHolder getViewHolder(String path) {
        return mHolders.get(path);
    }

    /**
     * ������·������ת��
     * 
     * @param path
     * @return
     */
    private String encodePath(String path) {
        return CommonTools.getEncodePath(path);
    }

    /**
     * ����������
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

    /**
     * �õ���Ӧ·�����ļ���С
     * 
     * @param path
     * @return
     */
    private int getFileSize(String path) {
        return new FileService(mContext).getFileSize(path);
    }

    /**
     * �����ݿ��еõ���Ӧ·�������صĴ�С
     * 
     * @param path
     * @return
     */
    private int getDownloadSize(String path) {
        FileService fileService = new FileService(mContext);
        Map<Integer, Integer> data = new ConcurrentHashMap<Integer, Integer>();
        Map<Integer, Integer> logdata = fileService
                .getData(path);// ��ȡ���ؼ�¼
        if (logdata.size() > 0) {// ����������ؼ�¼
            for (Map.Entry<Integer, Integer> entry : logdata.entrySet())
                data.put(entry.getKey(), entry.getValue());// �Ѹ����߳��Ѿ����ص����ݳ��ȷ���data��
        }
        int size = 0;
        for (int i = 0; i < data.size(); i++) {
            size += data.get(i + 1);
        }
        return size;
    }

    public static void print(String str) {
        Log.i(DOWNLOAD, str);
    }

    public static class UrlBean {
        public UrlBean(String url, String name, String content, String heat, String size,
                Drawable icon) {
            this.url = url;
            this.name = name;
            this.content = content;
            this.heat = heat;
            this.size = size;
            this.icon = icon;
        }

        public String url;
        public String name;
        public String content;
        public String heat;
        public String size;
        public Drawable icon;
    }
}
