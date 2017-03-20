package com.reige.bubbleview.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.reige.bubbleview.Utils.GeometryUtil;



/**
 * Created by REIGE
 * Date :2017/3/20.
 */

public class BubbleView extends View {

    private Paint mPaint;//画笔
    private int mMeasuredHeight;//控件高
    private int mMeasuredWidth;//控件宽
    private PointF mStaticPointF;//不动点的坐标
    private PointF mMovePointF;//移动点的坐标
    private double mLineK;//斜率

    private static final float RADIUS = 30; //初始化的半径大小

    private double mStaticRadius; //绘制时用到的半径

    //能够拖动的最大值
    private static final float MAX_DISTANCE = 300;

    private Path path;

    //是否超出过最大范围
    private boolean mIsOutOfRange = false;

    public BubbleView(Context context) {
        super(context);
        init();
    }

    public BubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasuredHeight = getMeasuredHeight();
        mMeasuredWidth = getMeasuredWidth();
        mStaticPointF.set(mMeasuredWidth / 2, mMeasuredHeight / 2);
        mMovePointF.set(mMeasuredWidth / 2, mMeasuredHeight / 2);
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(android.R.color.holo_red_light));
        mStaticPointF = new PointF(mMeasuredWidth / 2, mMeasuredHeight / 2);
        mMovePointF = new PointF(mMeasuredWidth / 2, mMeasuredHeight / 2);
        mStaticRadius = RADIUS;
        mStaticPointF = new PointF(mMeasuredWidth / 2, mMeasuredHeight / 2);
        mMovePointF = new PointF(mMeasuredWidth / 2, mMeasuredHeight / 2);
        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //重置path
        path.reset();
        if (!mIsOutOfRange) {
            canvas.drawCircle(mStaticPointF.x, mStaticPointF.y, (float) mStaticRadius, mPaint);

            float dx = mMovePointF.x - mStaticPointF.x;
            float dy = mMovePointF.y - mStaticPointF.y;
            if (dx != 0) {
                mLineK = dy / dx;
            }
            PointF[] staticPoints = GeometryUtil.getIntersectionPoints(mStaticPointF, (float)
                    mStaticRadius, mLineK);
            PointF[] movePoints = GeometryUtil.getIntersectionPoints(mMovePointF, 30, mLineK);
            PointF controlPoint = GeometryUtil.getPointByPercent(mStaticPointF, mMovePointF, 0.5f);

            path.moveTo(staticPoints[0].x, staticPoints[0].y);
            path.lineTo(staticPoints[1].x, staticPoints[1].y);
            path.quadTo(controlPoint.x, controlPoint.y, movePoints[1].x, movePoints[1].y);
            path.lineTo(movePoints[0].x, movePoints[0].y);
            path.quadTo(controlPoint.x, controlPoint.y, staticPoints[0].x, staticPoints[0].y);

            canvas.drawPath(path, mPaint);
            path.close();
        }

        canvas.drawCircle(mMovePointF.x, mMovePointF.y, 30, mPaint);

    }

    /**
     * 根据两圆的距离 计算中心固定圆半径
     * @param distance 两圆的距离
     */
    private void getStaticRadius(float distance) {
        mStaticRadius = RADIUS * (0.8 * (MAX_DISTANCE - distance) / MAX_DISTANCE + 0.2);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float distance = GeometryUtil.getDistanceBetween2Points(mStaticPointF, mMovePointF);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX();
                float dy = event.getY();
                mMovePointF.set(dx, dy);

                if (distance > MAX_DISTANCE) {
                    distance = MAX_DISTANCE;
                    mIsOutOfRange = true;
                } else {

                }
                getStaticRadius(distance);

                break;
            case MotionEvent.ACTION_UP:
                if (distance > MAX_DISTANCE) {

                } else {

                    if (mIsOutOfRange) {
                        mMovePointF.set(mStaticPointF);
                        mIsOutOfRange = false;
                    } else {
                        mIsOutOfRange = false;
                        ValueAnimator valueAnimator = ObjectAnimator.ofFloat(1);
                        final PointF startPointF = new PointF(mMovePointF.x, mMovePointF.y);
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                //获得动画执行的百分比
                                float animatedFraction = valueAnimator.getAnimatedFraction();

                                PointF pointByPercent = GeometryUtil.getPointByPercent
                                        (startPointF, mStaticPointF, animatedFraction);
                                mMovePointF.set(pointByPercent);
                                float distance = GeometryUtil.getDistanceBetween2Points
                                        (mStaticPointF, mMovePointF);
                                getStaticRadius(distance);
                                invalidate();
                            }
                        });
                        valueAnimator.setDuration(500);
                        //添加一个插补器 OvershootInterpolator
                        valueAnimator.setInterpolator(new OvershootInterpolator(3));
                        valueAnimator.start();

                    }

                }

                break;
        }
        invalidate();
        return true;
    }



}
