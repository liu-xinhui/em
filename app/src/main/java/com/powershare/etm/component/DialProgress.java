package com.powershare.etm.component;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.blankj.utilcode.util.SizeUtils;
import com.powershare.etm.R;

/**
 * https://github.com/jenly1314/CircleProgressView
 */
public class DialProgress extends View {
    /**
     * 画笔
     */
    private Paint mPaint;

    /**
     * 文本画笔
     */
    private TextPaint mTextPaint;

    /**
     * 笔画描边的宽度
     */
    private float mStrokeWidth;

    /**
     * 开始角度(默认从12点钟方向开始)
     */
    private int mStartAngle = 270;
    /**
     * 扫描角度(一个圆)
     */
    private int mSweepAngle = 360;

    /**
     * 圆心坐标x
     */
    private float mCircleCenterX;
    /**
     * 圆心坐标y
     */
    private float mCircleCenterY;

    /**
     * 圆正常颜色
     */
    private int mNormalColor = 0xFFC8C8C8;
    /**
     * 进度颜色
     */
    private int mProgressColor = 0xFF4FEAAC;

    /**
     * 是否使用着色器
     */
    private boolean isShader = true;

    /**
     * 着色器
     */
    private Shader mShader;

    /**
     * 着色器颜色
     */
    private int[] mShaderColors = new int[]{0xBB00F4FF, 0xBB9523C5, 0xBBF44B57, 0xBB9523C5, 0xBB00F4FF};

    /**
     * 半径
     */
    private float mRadius;

    /**
     * 内圆与外圆的间距
     */
    private float mCirclePadding;

    /**
     * 刻度间隔的角度大小
     */
    private float mTickSplitAngle = 1.8f;

    /**
     * 刻度的角度大小
     */
    private float mBlockAngle = 1;

    /**
     * 总刻度数
     */
    private int mTotalTickCount;

    /**
     * 最大进度
     */
    private int mMax = 100;

    /**
     * 当前进度
     */
    private int mProgress = 0;

    /**
     * 动画持续的时间
     */
    private int mDuration = 500;

    /**
     * 标签内容
     */
    private String mLabelText;

    /**
     * 字体大小
     */
    private float mLabelTextSize;

    /**
     * 字体颜色
     */
    private int mLabelTextColor = 0xFF333333;
    /**
     * 进度百分比
     */
    private int mProgressPercent;

    /**
     * 是否显示标签文字
     */
    private boolean isShowLabel = true;
    /**
     * 是否默认显示百分比为标签文字
     */
    private boolean isShowPercentText = true;

    private OnChangeListener mOnChangeListener;

    public DialProgress(Context context) {
        this(context, null);
    }

    public DialProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        DisplayMetrics displayMetrics = getDisplayMetrics();
        mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, displayMetrics);
        mLabelTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 30, displayMetrics);
        mCirclePadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, displayMetrics);

        mStrokeWidth = SizeUtils.dp2px(12);
        isShader = false;
        mStartAngle = 270;
        mSweepAngle = 360;
        mMax = 100;
        mProgress = 0;
        mDuration = 500;
        mLabelText = "234";
        mLabelTextSize = 30;
        isShowLabel = false;
        mCirclePadding = 10;
        mBlockAngle = 1;
        isShowPercentText = TextUtils.isEmpty(mLabelText);
        mProgressPercent = (int) (mProgress * 100.0f / mMax);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        //mPaint.setShadowLayer(30, 0, 0, Color.RED);
        //mPaint.setMaskFilter(new BlurMaskFilter(30f, BlurMaskFilter.Blur.SOLID));
        mTextPaint = new TextPaint();
        mTotalTickCount = (int) (mSweepAngle / (mTickSplitAngle + mBlockAngle));
        anim();
    }

    private DisplayMetrics getDisplayMetrics() {
        return getResources().getDisplayMetrics();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int defaultValue = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getDisplayMetrics());

        int width = measureHandler(widthMeasureSpec, defaultValue);
        int height = measureHandler(heightMeasureSpec, defaultValue);

        //圆心坐标
        mCircleCenterX = (width + getPaddingLeft() - getPaddingRight()) / 2.0f;
        mCircleCenterY = (height + getPaddingTop() - getPaddingBottom()) / 2.0f;
        //计算间距
        int padding = Math.max(getPaddingLeft() + getPaddingRight(), getPaddingTop() + getPaddingBottom());
        //半径=视图宽度-横向或纵向内间距值 - 画笔宽度
        mRadius = (width - padding - mStrokeWidth) / 2.0f - mCirclePadding;
        //默认着色器
        mShader = new SweepGradient(mCircleCenterX, mCircleCenterX, mShaderColors, null);
        setMeasuredDimension(width, height);
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawArc(canvas);
        //drawText(canvas);
        drawPointer(canvas);
    }

    private void anim() {
        /*ValueAnimator animator = ValueAnimator.ofFloat(0, 360);
        animator.setDuration(1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(valueAnimator -> {
            angle = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        animator.start();*/
    }

    private int progress = 50;
    private int dis = SizeUtils.dp2px(10);

    private void drawPointer(Canvas canvas) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pointer);
        Matrix m = new Matrix();
        float px = getWidth() >> 1;
        float py = getHeight() >> 1;
        float tempY = bitmap.getHeight() >> 1;
        m.setRotate(progress * 2.78f, bitmap.getWidth() - dis, tempY);
        m.postTranslate(px - bitmap.getWidth() + dis, py - tempY);
        canvas.save();
        canvas.rotate(-49, px, py);
        canvas.drawBitmap(bitmap, m, null);
        canvas.restore();
    }

    /**
     * 绘制弧形(默认为一个圆)
     */
    private RectF rectF;

    private void drawArc(Canvas canvas) {
        if (rectF == null) {
            float tickDiameter = mRadius * 2;
            float tickStartX = mCircleCenterX - mRadius;
            float tickStartY = mCircleCenterY - mRadius;
            rectF = new RectF(tickStartX, tickStartY, tickStartX + tickDiameter, tickStartY + tickDiameter);
        }
        final int currentBlockIndex = (int) (mProgressPercent / 100f * mTotalTickCount);
        for (int i = 0; i < mTotalTickCount; i++) {
            if (i < 28) {
                continue;
            }
            if (i < currentBlockIndex) {
                //已选中的刻度
                if (isShader && mShader != null) {
                    mPaint.setShader(mShader);
                } else {
                    mPaint.setColor(mProgressColor);
                }
            } else {
                //未选中的刻度
                mPaint.setShader(null);
                mPaint.setColor(mNormalColor);
            }
            //绘制外边框刻度
            canvas.drawArc(rectF, i * (mBlockAngle + mTickSplitAngle) + mStartAngle + 142.5f, mBlockAngle, false, mPaint);
        }
    }

    /**
     * 绘制中间的文本
     */
    private void drawText(Canvas canvas) {
        if (!isShowLabel) {
            return;
        }
        mTextPaint.reset();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setTextSize(mLabelTextSize);
        mTextPaint.setColor(mLabelTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        // 计算文字高度 
        float fontHeight = fontMetrics.bottom - fontMetrics.top;
        // 计算文字baseline 
        float textBaseY = getHeight() - (getHeight() - fontHeight) / 2 - fontMetrics.bottom;

        int textX = getWidth() / 2;
        if (isShowPercentText) {//是否显示百分比
            canvas.drawText(mProgressPercent + "%", textX, textBaseY, mTextPaint);
        } else if (!TextUtils.isEmpty(mLabelText)) {//显示自定义文本
            canvas.drawText(mLabelText, textX, textBaseY, mTextPaint);
        }
    }

    /**
     * 显示进度动画效果（根据当前已有进度开始）
     */
    public void showAppendAnimation(int progress) {
        showAnimation(mProgress, progress, mDuration);
    }

    /**
     * 显示进度动画效果
     */
    public void showAnimation(int progress) {
        showAnimation(progress, mDuration);
    }

    /**
     * 显示进度动画效果
     *
     * @param duration 动画时长
     */
    public void showAnimation(int progress, int duration) {
        showAnimation(0, progress, duration);
    }

    /**
     * 显示进度动画效果，从from到to变化
     *
     * @param duration 动画时长
     */
    public void showAnimation(int from, int to, int duration) {
        showAnimation(from, to, duration, null);
    }

    /**
     * 显示进度动画效果，从from到to变化
     *
     * @param duration 动画时长
     */
    public void showAnimation(int from, int to, int duration, Animator.AnimatorListener listener) {
        this.mDuration = duration;
        this.mProgress = from;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(from, to);
        valueAnimator.setDuration(duration);

        valueAnimator.addUpdateListener(animation -> setProgress((int) animation.getAnimatedValue()));

        if (listener != null) {
            valueAnimator.removeAllUpdateListeners();
            valueAnimator.addListener(listener);
        }
        valueAnimator.start();
    }

    /**
     * 设置最大进度
     */
    public void setMax(int max) {
        this.mMax = max;
        invalidate();
    }

    /**
     * 设置当前进度
     */
    public void setProgress(int progress) {
        this.mProgress = progress;
        mProgressPercent = (int) (mProgress * 100.0f / mMax);
        invalidate();

        if (mOnChangeListener != null) {
            mOnChangeListener.onProgressChanged(mProgress, mMax);
        }
    }

    /**
     * 设置正常颜色
     */
    public void setNormalColor(int color) {
        this.mNormalColor = color;
        invalidate();
    }


    /**
     * 设置着色器
     */
    public void setShader(Shader shader) {
        isShader = true;
        this.mShader = shader;
        invalidate();
    }

    /**
     * 设置进度颜色（通过着色器实现渐变色）
     */
    public void setProgressColor(int... colors) {
        this.mShaderColors = colors;
        Shader shader = new SweepGradient(mCircleCenterX, mCircleCenterX, colors, null);
        setShader(shader);
    }

    /**
     * 设置进度颜色（纯色）
     */
    public void setProgressColor(int color) {
        isShader = false;
        this.mProgressColor = color;
        invalidate();
    }

    /**
     * 设置进度颜色
     */
    public void setProgressColorResource(int resId) {
        int color = getResources().getColor(resId);
        setProgressColor(color);
    }

    public int getStartAngle() {
        return mStartAngle;
    }

    public int getSweepAngle() {
        return mSweepAngle;
    }

    public float getCircleCenterX() {
        return mCircleCenterX;
    }

    public float getCircleCenterY() {
        return mCircleCenterY;
    }

    public float getRadius() {
        return mRadius;
    }

    public int getMax() {
        return mMax;
    }

    public int getProgress() {
        return mProgress;
    }

    public String getLabelText() {
        return mLabelText;
    }

    /**
     * 设置标签文本
     */
    public void setLabelText(String labelText) {
        this.mLabelText = labelText;
        this.isShowPercentText = TextUtils.isEmpty(labelText);
        invalidate();
    }

    /**
     * 进度百分比
     */
    public int getProgressPercent() {
        return mProgressPercent;
    }

    /**
     * 如果自定义设置过{@link #setLabelText(String)} 或通过xml设置过{@code app:labelText}则
     * 返回{@link #mLabelText}，反之默认返回百分比{@link #mProgressPercent}
     */
    public String getText() {
        if (isShowPercentText) {
            return mProgressPercent + "%";
        }

        return mLabelText;
    }

    public int getLabelTextColor() {
        return mLabelTextColor;
    }

    public void setLabelTextColor(int color) {
        this.mLabelTextColor = color;
        invalidate();
    }

    public void setLabelTextColorResource(int resId) {
        int color = getResources().getColor(resId);
        setLabelTextColor(color);
    }

    public void setLabelTextSize(float textSize) {
        setLabelTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    public void setLabelTextSize(int unit, float textSize) {
        float size = TypedValue.applyDimension(unit, textSize, getDisplayMetrics());
        if (mLabelTextSize != size) {
            this.mLabelTextSize = size;
            invalidate();
        }

    }

    /**
     * 设置进度改变监听
     */
    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.mOnChangeListener = onChangeListener;
    }

    public interface OnChangeListener {
        void onProgressChanged(float progress, float max);
    }
}
