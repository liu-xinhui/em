package com.powershare.etm.component;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.AppCompatButton;

import com.blankj.utilcode.util.SizeUtils;
import com.noober.background.BackgroundFactory;

public class LoadingButton extends AppCompatButton {
    private Paint mPaint;
    private PathMeasure measure;
    private float mAnimValue;
    private Path mTempPath;
    private ValueAnimator animator;
    private int textColor;
    private boolean isLoading;

    public LoadingButton(Context context) {
        super(context);
        init(context, null);
    }

    public LoadingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LoadingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            BackgroundFactory.setViewBackground(context, attrs, this);
        }
        //loading画笔
        mPaint = new Paint();
        mPaint.setColor(getTextColors().getDefaultColor());
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(SizeUtils.dp2px(2));
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void showLoading() {
        isLoading = true;
        setEnabled(false);
        textColor = getCurrentTextColor();
        setTextColor(Color.TRANSPARENT);
        if (animator == null) {
            animator = ValueAnimator.ofFloat(0, 1);
            animator.setDuration(1000);
            animator.setInterpolator(new LinearInterpolator());
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.addUpdateListener(valueAnimator -> {
                mAnimValue = (float) valueAnimator.getAnimatedValue();
                invalidate();
            });
        }
        animator.start();
    }

    public void hideLoading() {
        isLoading = false;
        setEnabled(true);
        setTextColor(textColor);
        if (animator != null) {
            animator.end();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isLoading) {
            return;
        }
        if (measure == null) {
            int radius = getHeight() / 5;
            int x = getWidth() / 2;
            int y = getHeight() / 2;
            //新建一个路径
            Path path = new Path();
            //添加圆为路径 参数 ： x坐标 y坐标 半径 顺势针/逆时针
            path.addCircle(x, y, radius, Path.Direction.CW);
            //这是我们今天的主角，PathMeasure 关联上面的圆。 forceClosed 为true
            measure = new PathMeasure(path, true);
            mTempPath = new Path();
        }
        //loading
        float pathLength = measure.getLength();
        float end = pathLength * mAnimValue;
        float start = (float) (end - ((0.5 - Math.abs(mAnimValue - 0.5)) * pathLength));
        mTempPath.reset();
        //通过PathMeasure的getSegment方法截取一段路径保存在dst路径中
        measure.getSegment(start, end, mTempPath, true);
        //画出截取的路径
        canvas.drawPath(mTempPath, mPaint);
    }
}
