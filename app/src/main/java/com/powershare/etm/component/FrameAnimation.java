package com.powershare.etm.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class FrameAnimation extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private SurfaceHolder mSurfaceHolder;
    private boolean mIsThreadRunning = true; // 线程运行开关
    private boolean mIsDestroy = false;// 是否已经销毁
    private String[] mBitmapUrls;// 用于播放动画的图片资源数组
    private int mCurrentIndex;// 当前动画播放的位置
    private int mGapTime = 20;// 每帧动画持续存在的时间
    private OnFrameFinishedListener mOnFrameFinishedListener;// 动画监听事件
    private List<Bitmap> bitmaps;

    public FrameAnimation(Context context) {
        this(context, null);
    }

    public FrameAnimation(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);// 注册回调方法
        // 白色背景
        setZOrderOnTop(true);
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
    }

    public FrameAnimation(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 创建surfaceView时启动线程
        // new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 当surfaceView销毁时, 停止线程的运行. 避免surfaceView销毁了线程还在运行而报错.
        mIsThreadRunning = false;
        try {
            Thread.sleep(mGapTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mIsDestroy = true;
    }

    /**
     * 制图方法
     */
    private void drawBitmap() {
        // 无资源文件退出
        if (mBitmapUrls == null) {
            mIsThreadRunning = false;
            return;
        }
        LogUtils.e("---------------1");
        // 锁定画布
        Canvas canvas = mSurfaceHolder.lockCanvas();
        Bitmap bitmap = null;
        try {
            if (mSurfaceHolder != null && canvas != null) {
                canvas.drawColor(Color.WHITE);
                // 如果图片过大可以再次对图片进行二次采样缩放处理
                //bitmap = BitmapFactory.decodeResource(getResources(), mBitmapResourceIds[mCurrentIndex]);
                LogUtils.e(mCurrentIndex + "---------------1111");
                bitmap = Glide.with(getContext())
                        .asBitmap()
                        .skipMemoryCache(false)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .load(mBitmapUrls[mCurrentIndex])
                        .submit(800, 470).get();
                //bitmap = bitmaps.get(mCurrentIndex);
                LogUtils.e("---------------2");
                int left = (getWidth() - bitmap.getWidth()) / 2;
                int top = (getHeight() - bitmap.getHeight()) / 2;
                canvas.drawBitmap(bitmap, left, top, null);
                LogUtils.e("---------------3");
                // 播放到最后一张图片，停止线程
                if (mCurrentIndex == mBitmapUrls.length - 1) {
                    mIsThreadRunning = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mCurrentIndex++;
            if (mSurfaceHolder != null && canvas != null) {
                // 将画布解锁并显示在屏幕上
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
            if (bitmap != null) {
                // 收回图片
                bitmap.recycle();
            }
        }
    }

    @Override
    public void run() {
        if (mOnFrameFinishedListener != null) {
            mOnFrameFinishedListener.onStart();
        }
        // 每隔100ms刷新屏幕
        while (mIsThreadRunning) {
            drawBitmap();
            try {
                Thread.sleep(mGapTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mOnFrameFinishedListener != null) {
            mOnFrameFinishedListener.onStop();
        }
    }

    /**
     * 开始动画
     */
    public void start() {
        if (!mIsDestroy) {
            mCurrentIndex = 0;
            mIsThreadRunning = true;
            new Thread(this).start();
        } else {
            // 如果SurfaceHolder已经销毁抛出该异常
            throw new RuntimeException("IllegalArgumentException:Are you sure the SurfaceHolder is not destroyed");
        }
    }

    /**
     * 设置动画播放素材
     */
    public void setBitmapUrls(String[] urls) {
        this.mBitmapUrls = urls;
        bitmaps = new ArrayList<>();
        start();
        /*new Thread(() -> {
            try {
                for (String url : urls) {
                    LogUtils.d(url);
                    Bitmap bitmap = Glide.with(getContext())
                            .asBitmap()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .load(url)
                            .submit(800,470).get();
                    bitmaps.add(bitmap);
                }
                LogUtils.d("完成");
                start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();*/
    }


    /**
     * 设置每帧时间
     */
    public void setGapTime(int gapTime) {
        this.mGapTime = gapTime;
    }

    /**
     * 结束动画
     */
    public void stop() {
        mIsThreadRunning = false;
    }

    /**
     * 设置动画监听器
     */
    public void setOnFrameFinishedListener(OnFrameFinishedListener onFrameFinishedListener) {
        this.mOnFrameFinishedListener = onFrameFinishedListener;
    }

    /**
     * 动画监听器
     */
    public interface OnFrameFinishedListener {
        /**
         * 动画开始
         */
        void onStart();

        /**
         * 动画结束
         */
        void onStop();
    }

    /**
     * 当用户点击返回按钮时，停止线程，反转内存溢出
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 当按返回键时，将线程停止，避免surfaceView销毁了,而线程还在运行而报错
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mIsThreadRunning = false;
        }
        return super.onKeyDown(keyCode, event);
    }

}