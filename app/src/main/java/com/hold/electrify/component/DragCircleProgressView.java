package com.hold.electrify.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.SizeUtils;
import com.hold.electrify.R;

/**
 * 自定义绚丽的ProgressBar.
 */
public class DragCircleProgressView extends View {
    /**
     * 进度条所占用的角度
     */
    private static final int ARC_FULL_DEGREE = 300;
    /**
     * 弧线的宽度
     */
    private int STROKE_WIDTH;
    /**
     * 组件的宽，高
     */
    private int width, height;
    /**
     * 进度条最大值和当前进度值
     */
    private float max, progress;
    /**
     * 是否允许拖动进度条
     */
    private boolean draggingEnabled = false;
    /**
     * 绘制弧线的矩形区域
     */
    private RectF circleRectF;
    /**
     * 绘制弧线的画笔
     */
    private Paint progressPaint;
    /**
     * 绘制文字的画笔
     */
    private Paint textPaint;
    /**
     * 绘制当前进度值的画笔
     */
    private Paint thumbPaint;
    /**
     * 圆弧的半径
     */
    private int circleRadius;
    /**
     * 圆弧圆心位置
     */
    private int centerX, centerY;

    private int colorAccent = ContextCompat.getColor(getContext(), R.color.colorAccent);

    public DragCircleProgressView(Context context) {
        super(context);
        init();
    }

    public DragCircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragCircleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        textPaint = new Paint();
        textPaint.setColor(colorAccent);
        textPaint.setAntiAlias(true);
        thumbPaint = new Paint();
        thumbPaint.setAntiAlias(true);

        this.setOnTouchListener((view, motionEvent) -> {
            getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
    }

    private boolean isMeasure = false;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int defaultValue = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getDisplayMetrics());
        if (!isMeasure) {
            isMeasure = true;
            width = measureHandler(widthMeasureSpec, defaultValue);
            height = measureHandler(heightMeasureSpec, defaultValue);
            //计算圆弧半径和圆心点
            circleRadius = Math.min(width, height) / 2 - SizeUtils.dp2px(10);
            STROKE_WIDTH = circleRadius / 7;
            circleRadius -= STROKE_WIDTH;
            centerX = width / 2;
            centerY = height / 2;
            //圆弧所在矩形区域
            circleRectF = new RectF();
            circleRectF.left = centerX - circleRadius;
            circleRectF.top = centerY - circleRadius;
            circleRectF.right = centerX + circleRadius;
            circleRectF.bottom = centerY + circleRadius;
        }
    }

    /**
     * 测量
     */
    private int measureHandler(int measureSpec, int defaultSize) {
        int result = defaultSize;
        int measureMode = MeasureSpec.getMode(measureSpec);
        int measureSize = MeasureSpec.getSize(measureSpec);
        if (measureMode == MeasureSpec.EXACTLY) {
            result = measureSize;
        } else if (measureMode == MeasureSpec.AT_MOST) {
            result = Math.min(defaultSize, measureSize);
        }
        return result;
    }

    private DisplayMetrics getDisplayMetrics() {
        return getResources().getDisplayMetrics();
    }

    private Rect textBounds = new Rect();
    private int dp5 = SizeUtils.dp2px(5);
    private int dp8 = SizeUtils.dp2px(8);
    private int dpText = SizeUtils.dp2px(55);

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float start = 90 + ((360 - ARC_FULL_DEGREE) >> 1); //进度条起始点
        float sweep1 = ARC_FULL_DEGREE * (progress / max); //进度划过的角度
        //float sweep2 = ARC_FULL_DEGREE - sweep1; //剩余的角度

        float radians = (float) (((360.0f - ARC_FULL_DEGREE) / 2) / 180 * Math.PI);
        //绘制进度条背景
        int bgWidth = STROKE_WIDTH + dp8;
        int bgWidth2 = bgWidth / 2;
        progressPaint.setStrokeWidth(bgWidth);
        progressPaint.setStyle(Paint.Style.STROKE);//设置空心
        progressPaint.setColor(Color.parseColor("#484848"));
        canvas.drawArc(circleRectF, start, 300, false, progressPaint);
        //绘制背景起始位置小圆形
        progressPaint.setStrokeWidth(0);
        progressPaint.setStyle(Paint.Style.FILL);
        float startX = centerX - circleRadius * (float) Math.sin(radians);
        float startY = centerY + circleRadius * (float) Math.cos(radians);
        canvas.drawCircle(startX, startY, bgWidth2, progressPaint);
        //绘制背景结束位置小圆形
        progressPaint.setStrokeWidth(0);
        progressPaint.setStyle(Paint.Style.FILL);
        float endX = centerX + circleRadius * (float) Math.sin(radians);
        float endY = centerY + circleRadius * (float) Math.cos(radians);
        canvas.drawCircle(endX, endY, bgWidth2, progressPaint);

        //绘制进度条
        progressPaint.setColor(Color.parseColor("#3574F3"));
        progressPaint.setStrokeWidth(STROKE_WIDTH);
        progressPaint.setStyle(Paint.Style.STROKE);//设置空心
        canvas.drawArc(circleRectF, start, sweep1, false, progressPaint);
        //绘制进度条起始位置小圆形
        progressPaint.setStrokeWidth(0);
        progressPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(startX, startY, STROKE_WIDTH / 2, progressPaint);

        //文字
        textPaint.setTextSize(dpText);
        String text = (int) progress - 20 + "";
        float textLen = textPaint.measureText(text);
        //计算文字高度
        textPaint.getTextBounds("8", 0, 1, textBounds);
        float h1 = textBounds.height();
        //% 前面的数字水平居中，适当调整
        //float extra = text.startsWith("1") ? -textPaint.measureText("1") / 2 : 0;
        float extra = 0;
        canvas.drawText(text, centerX - textLen / 2 + extra - dp5, centerY - dp5 + h1 / 2, textPaint);
        //百分号
        textPaint.setTextSize(circleRadius >> 2);
        canvas.drawText("℃", centerX + textLen / 2 + extra + 5 - dp5, centerY - dp5 + h1 / 2, textPaint);
        //绘制进度位置，也可以直接替换成一张图片
        float progressRadians = (float) (((360.0f - ARC_FULL_DEGREE) / 2 + sweep1) / 180 * Math.PI);
        float thumbX = centerX - circleRadius * (float) Math.sin(progressRadians);
        float thumbY = centerY + circleRadius * (float) Math.cos(progressRadians);
        thumbPaint.setColor(colorAccent);
        canvas.drawCircle(thumbX, thumbY, STROKE_WIDTH * 0.9f, thumbPaint);
    }

    private boolean isDragging = false;

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!draggingEnabled) {
            return super.onTouchEvent(event);
        }
        //处理拖动事件
        float currentX = event.getX();
        float currentY = event.getY();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //判断是否在进度条thumb位置
                if (checkOnArc(currentX, currentY)) {
                    float newProgress = calDegreeByPosition(currentX, currentY) / ARC_FULL_DEGREE * max;
                    setProgressSync(newProgress);
                    isDragging = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    //判断拖动时是否移出去了
                    if (checkOnArc(currentX, currentY)) {
                        setProgressSync(calDegreeByPosition(currentX, currentY) / ARC_FULL_DEGREE * max);
                    } else {
                        isDragging = false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isDragging = false;
                break;
        }
        return true;
    }

    private float calDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /**
     * 判断该点是否在弧线上（附近）
     */
    private boolean checkOnArc(float currentX, float currentY) {
        float distance = calDistance(currentX, currentY, centerX, centerY);
        float degree = calDegreeByPosition(currentX, currentY);
        return distance > circleRadius - STROKE_WIDTH * 5 && distance < circleRadius + STROKE_WIDTH * 5
                && (degree >= -8 && degree <= ARC_FULL_DEGREE + 8);
    }

    /**
     * 根据当前位置，计算出进度条已经转过的角度。
     */
    private float calDegreeByPosition(float currentX, float currentY) {
        float a1 = (float) (Math.atan(1.0f * (centerX - currentX) / (currentY - centerY)) / Math.PI * 180);
        if (currentY < centerY) {
            a1 += 180;
        } else if (currentY > centerY && currentX > centerX) {
            a1 += 360;
        }
        return a1 - (360 - ARC_FULL_DEGREE) / 2;
    }

    public void setMax(int max) {
        this.max = max;
        invalidate();
    }

    public void setProgress(float progress) {
        final float validProgress = checkProgress(progress);
        //动画切换进度值
        new Thread(() -> {
            float oldProgress = DragCircleProgressView.this.progress;
            for (int i = 1; i <= 100; i++) {
                DragCircleProgressView.this.progress = oldProgress + (validProgress - oldProgress) * (1.0f * i / 100);
                postInvalidate();
                SystemClock.sleep(20);
            }
        }).start();
    }

    public void setProgressSync(float progress) {
        this.progress = checkProgress(progress);
        invalidate();
    }

    //保证progress的值位于[0,max]
    private float checkProgress(float progress) {
        if (progress < 0) {
            return 0;
        }
        return Math.min(progress, max);
    }

    public void setDraggingEnabled(boolean draggingEnabled) {
        this.draggingEnabled = draggingEnabled;
    }
}