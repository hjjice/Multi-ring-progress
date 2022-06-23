package com.hearing.aid.diy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.hearing.aid.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 圆盘计步图表
 */
public class MoreCircleProgressBar extends View {
    private List<RectF> mWheelRectLs = new ArrayList<>();
    private List<Paint> mFinishWheelPaintLs = new ArrayList<>();
    private Paint mCenterWheelPaint;
    private float mCircleStrokeWidth;
    private List<Float> mSweepAnglePerLs = new ArrayList<>();
    private List<Float> mPercentLs = new ArrayList<>();
    private List<Integer> mStepNumLs = new ArrayList<>();
    private List<Integer> mCurrStepNumLs = new ArrayList<>();
    private int currentLevel = 9;
    private float pressExtraStrokeWidth;
    private BarAnimation mAnim;
    private int mMaxStepNum = 41;// 默认最大步数
    private DecimalFormat mDecimalFormat = new DecimalFormat("#.0");// 格式为保留小数点后一位
    public static String GOAL_STEP;
    public static String PERCENT;

    public MoreCircleProgressBar(Context context) {
        super(context);
        init(null, 0);
    }

    public MoreCircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MoreCircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        for (int i = 0; i < 10; i++) {
            RectF mWheelRect = new RectF();
            mWheelRectLs.add(mWheelRect);
            mStepNumLs.add(0);
            mCurrStepNumLs.add(0);
            mSweepAnglePerLs.add(-1f);
            mPercentLs.add(0f);

            Paint mFinishWheelPaint = new Paint();
            mFinishWheelPaint.setColor(Color.rgb(100, 113, 205));
            mFinishWheelPaint.setStyle(Paint.Style.STROKE);// 空心
            mFinishWheelPaint.setStrokeCap(Paint.Cap.ROUND);// 圆角画笔
            mFinishWheelPaint.setAntiAlias(true);// 去锯齿
            mFinishWheelPaintLs.add(mFinishWheelPaint);
        }

        mCenterWheelPaint = new Paint();
        mCenterWheelPaint.setColor(Color.rgb(243, 243, 243));
        mCenterWheelPaint.setStyle(Paint.Style.STROKE);
        mCenterWheelPaint.setStrokeCap(Paint.Cap.ROUND);
        mCenterWheelPaint.setAntiAlias(true);

        mAnim = new BarAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < 10; i++) {
            float percent = getPercent(i);
            if (percent < 60) {
                mFinishWheelPaintLs.get(i).setColor(getResources().getColor(R.color.red));
            } else if (percent >= 60 && percent <= 80) {
                mFinishWheelPaintLs.get(i).setColor(getResources().getColor(R.color.orange));
            } else {
                mFinishWheelPaintLs.get(i).setColor(getResources().getColor(R.color.colorPrimary));
            }

            canvas.drawArc(mWheelRectLs.get(i), 0, 359, false, mCenterWheelPaint);
            if (mSweepAnglePerLs.get(i) != -1) {
                canvas.drawArc(mWheelRectLs.get(i), 90, mSweepAnglePerLs.get(i), false, mFinishWheelPaintLs.get(i));
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int min = Math.min(width, height);// 获取View最短边的长度
        setMeasuredDimension(min, min);// 强制改View为以最短边为长度的正方形
        mCircleStrokeWidth = getTextScale(15, min);// 圆弧的宽度
        int sca = (int) (min / 30f);
        for (int j = 0; j < mWheelRectLs.size(); j++) {
            pressExtraStrokeWidth = getTextScale(sca * j, min);// 圆弧离矩形的距离
            mWheelRectLs.get(j).set(mCircleStrokeWidth + pressExtraStrokeWidth, mCircleStrokeWidth + pressExtraStrokeWidth,
                    min - mCircleStrokeWidth - pressExtraStrokeWidth, min - mCircleStrokeWidth - pressExtraStrokeWidth);// 设置矩形
            mFinishWheelPaintLs.get(j).setStrokeWidth(mCircleStrokeWidth);
        }

        mCenterWheelPaint.setStrokeWidth(mCircleStrokeWidth);
    }

    /**
     * 进度条动画
     *
     * @author Administrator
     */
    public class BarAnimation extends Animation {

        /**
         * 每次系统调用这个方法时， 改变mSweepAnglePer，mPercent，stepnumbernow的值，
         * 然后调用postInvalidate()不停的绘制view。
         */
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            mPercentLs.set(currentLevel, Float.parseFloat(mDecimalFormat.format(mStepNumLs.get(currentLevel) * 100f / mMaxStepNum))); // 将浮点值四舍五入保留一位小数
            if (mPercentLs.get(currentLevel) > 100.0f) {
                mPercentLs.set(currentLevel, 100.0f);
            }
            PERCENT = String.valueOf(mPercentLs.get(currentLevel));
            mSweepAnglePerLs.set(currentLevel, (float) (mStepNumLs.get(currentLevel) * 360 / mMaxStepNum));
            mCurrStepNumLs.set(currentLevel, mStepNumLs.get(currentLevel));

            requestLayout();
        }
    }

    /**
     * 根据控件的大小改变绝对位置的比例
     *
     * @param n
     * @param m
     * @return
     */
    public float getTextScale(float n, float m) {
        return n / 500 * m;
    }

    /**
     * 更新步数和设置一圈动画时间
     *
     * @param stepCount
     * @param time
     */
    public void update(int stepCount, int time) {
        this.mStepNumLs.set(currentLevel, stepCount);
        mAnim.setDuration(time);
        // setAnimationTime(time);
        this.startAnimation(mAnim);
    }

    public void addPro() {
        if (mStepNumLs.get(currentLevel) < mMaxStepNum) {
            int pro = mStepNumLs.get(currentLevel) + 1;
            this.mStepNumLs.set(currentLevel, pro);
        }

        mAnim.setDuration(200);
        this.startAnimation(mAnim);
    }

    public void reducePro() {
        if (mStepNumLs.get(currentLevel) > 0) {
            int pro = mStepNumLs.get(currentLevel) - 1;
            this.mStepNumLs.set(currentLevel, pro);
        }

        mAnim.setDuration(200);
        this.startAnimation(mAnim);
    }

    public void reduceLevel() {
        if (currentLevel < 9) {
            currentLevel++;
            if (mSweepAnglePerLs.get(currentLevel) == -1) {
                this.mStepNumLs.set(currentLevel, 12);
                mAnim.setDuration(200);
                this.startAnimation(mAnim);
            }
        }
    }

    public void addLevel() {
        if (currentLevel > 0) {
            currentLevel--;
            if (mSweepAnglePerLs.get(currentLevel) == -1) {
                this.mStepNumLs.set(currentLevel, 12);
                mAnim.setDuration(200);
                this.startAnimation(mAnim);
            }
        }
    }

    public float getPercent(int index) {
        return mPercentLs.get(index);
    }

    /**
     * @param stepNum
     */
    public void setMaxStepNum(int stepNum) {
        mMaxStepNum = stepNum;
        GOAL_STEP = String.valueOf(mMaxStepNum);
    }

    public void setColor(int color) {
        mFinishWheelPaintLs.get(currentLevel).setColor(color);
    }

    //数值顺序是从外环到内环,如0对应的是10环
    public List<Integer> getAllProgress(){
        return mStepNumLs;
    }
}