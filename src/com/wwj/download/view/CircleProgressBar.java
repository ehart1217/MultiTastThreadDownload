
package com.wwj.download.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.TextView;

import com.wwj.download.R;

/**
 * 圆形进度圈
 * 
 * @author WanChi
 */
public class CircleProgressBar extends TextView {

    private final static String CIRCLE_GREEN = "#18b7c1";
    private final static String CIRCLE_RED = "#dc4242";

    /**
     * 作为按钮时，进度圈的状态，准备下载，正在下载，暂停，完成
     * 
     * @author WanChi
     */
    public enum BtnStatus {
        /** 准备下载就绪 */
        ready,
        /** 正在下载状态 */
        downloading,
        /** 暂停状态 */
        pause,
        /** 下载完成 */
        done
    };

    private String circleColor = CIRCLE_GREEN;// 正常状态
    private int maxProgress = 100;
    private int progress = 0;
    private int progressStrokeWidth = 3;
    private Context mContext;

    private BtnStatus mBtnStatus = BtnStatus.ready;
    // 画圆所在的距形区域
    RectF oval;
    Paint paint;

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO 自动生成的构造函数存根
        mContext = context;
        oval = new RectF();
        paint = new Paint();
        handleStatusChanged(mBtnStatus);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO 自动生成的方法存根
        super.onDraw(canvas);
        int width = this.getWidth();
        int height = this.getHeight();

        if (width != height)
        {
            int min = Math.min(width, height);
            width = min;
            height = min;
        }

        paint.setAntiAlias(true); // 设置画笔为抗锯齿
        paint.setColor(Color.WHITE); // 设置画笔颜色
        // canvas.drawColor(Color.WHITE); // 灰色背景
        paint.setStrokeWidth(progressStrokeWidth); // 线宽
        paint.setStyle(Style.STROKE);

        oval.left = progressStrokeWidth / 2; // 左上角x
        oval.top = progressStrokeWidth / 2; // 左上角y
        oval.right = width - progressStrokeWidth / 2; // 左下角x
        oval.bottom = height - progressStrokeWidth / 2; // 右下角y

        // canvas.drawArc(oval, -90, 360, false, paint); // 绘制白色圆圈，即进度条背景
        paint.setColor(Color.parseColor(circleColor));
        canvas.drawArc(oval, -90, ((float) progress / maxProgress) * 360, false, paint); // 绘制进度圆弧

        if (mBtnStatus.equals(BtnStatus.done)) {
            // paint.setStrokeWidth(1);
            // String text = "安装";
            // int textHeight = height / 3;
            // paint.setTextSize(textHeight);
            // int textWidth = (int) paint.measureText(text, 0, text.length());
            // paint.setStyle(Style.FILL);
            // canvas.drawText(text, width / 2 - textWidth / 2, height / 2 +
            // textHeight / 2, paint);
            this.setText("安装");
        } else {
            this.setText("");
        }

    }

    public int getMax() {
        return maxProgress;
    }

    public void setMax(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        this.invalidate();
    }

    public int getProgress() {
        return progress;
    }

    /**
     * 设置点击状态，并更新背景
     * 
     * @param btnStatus 枚举类型 {@link #BtnStatus} : ready, downloading, pause,
     *            done
     */
    public void setStatus(BtnStatus btnStatus) {
        mBtnStatus = btnStatus;
        handleStatusChanged(mBtnStatus);
    }

    public BtnStatus getStatus() {
        return mBtnStatus;
    }

    /**
     * 处理状态改变
     */
    @SuppressLint("NewApi")
    private void handleStatusChanged(BtnStatus btnStatus) {
        switch (btnStatus) {
            case ready:
                this.setBackground(mContext.getResources().getDrawable(R.drawable.download));
                break;
            case downloading:
                this.setBackground(mContext.getResources().getDrawable(R.drawable.download_pause));
                circleColor = CIRCLE_GREEN;
                break;
            case pause:
                this.setBackground(mContext.getResources().getDrawable(R.drawable.download_pause));
                circleColor = CIRCLE_RED;
                break;
            case done:
                this.setBackground(mContext.getResources().getDrawable(R.drawable.download_install));
                break;
            default:
                this.setBackground(mContext.getResources().getDrawable(R.drawable.download));
                break;
        }
        this.invalidate();
    }

    /**
     * 非ＵＩ线程调用
     */
    public void setProgressNotInUiThread(int progress) {
        this.progress = progress;
        this.postInvalidate();
    }

    // 手动写“安装”
    public void writeInstall() {

    }
}
