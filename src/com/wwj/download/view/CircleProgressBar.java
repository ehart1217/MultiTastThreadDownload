
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
 * Բ�ν���Ȧ
 * 
 * @author WanChi
 */
public class CircleProgressBar extends TextView {

    private final static String CIRCLE_GREEN = "#18b7c1";
    private final static String CIRCLE_RED = "#dc4242";

    /**
     * ��Ϊ��ťʱ������Ȧ��״̬��׼�����أ��������أ���ͣ�����
     * 
     * @author WanChi
     */
    public enum BtnStatus {
        /** ׼�����ؾ��� */
        ready,
        /** ��������״̬ */
        downloading,
        /** ��ͣ״̬ */
        pause,
        /** ������� */
        done
    };

    private String circleColor = CIRCLE_GREEN;// ����״̬
    private int maxProgress = 100;
    private int progress = 0;
    private int progressStrokeWidth = 3;
    private Context mContext;

    private BtnStatus mBtnStatus = BtnStatus.ready;
    // ��Բ���ڵľ�������
    RectF oval;
    Paint paint;

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO �Զ����ɵĹ��캯�����
        mContext = context;
        oval = new RectF();
        paint = new Paint();
        handleStatusChanged(mBtnStatus);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO �Զ����ɵķ������
        super.onDraw(canvas);
        int width = this.getWidth();
        int height = this.getHeight();

        if (width != height)
        {
            int min = Math.min(width, height);
            width = min;
            height = min;
        }

        paint.setAntiAlias(true); // ���û���Ϊ�����
        paint.setColor(Color.WHITE); // ���û�����ɫ
        // canvas.drawColor(Color.WHITE); // ��ɫ����
        paint.setStrokeWidth(progressStrokeWidth); // �߿�
        paint.setStyle(Style.STROKE);

        oval.left = progressStrokeWidth / 2; // ���Ͻ�x
        oval.top = progressStrokeWidth / 2; // ���Ͻ�y
        oval.right = width - progressStrokeWidth / 2; // ���½�x
        oval.bottom = height - progressStrokeWidth / 2; // ���½�y

        // canvas.drawArc(oval, -90, 360, false, paint); // ���ư�ɫԲȦ��������������
        paint.setColor(Color.parseColor(circleColor));
        canvas.drawArc(oval, -90, ((float) progress / maxProgress) * 360, false, paint); // ���ƽ���Բ��

        if (mBtnStatus.equals(BtnStatus.done)) {
            // paint.setStrokeWidth(1);
            // String text = "��װ";
            // int textHeight = height / 3;
            // paint.setTextSize(textHeight);
            // int textWidth = (int) paint.measureText(text, 0, text.length());
            // paint.setStyle(Style.FILL);
            // canvas.drawText(text, width / 2 - textWidth / 2, height / 2 +
            // textHeight / 2, paint);
            this.setText("��װ");
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
     * ���õ��״̬�������±���
     * 
     * @param btnStatus ö������ {@link #BtnStatus} : ready, downloading, pause,
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
     * ����״̬�ı�
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
     * �ǣգ��̵߳���
     */
    public void setProgressNotInUiThread(int progress) {
        this.progress = progress;
        this.postInvalidate();
    }

    // �ֶ�д����װ��
    public void writeInstall() {

    }
}
