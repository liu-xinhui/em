package com.powershare.etm.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.ImageCache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ImageView360 extends AppCompatImageView {

    private static int padding = SizeUtils.dp2px(70);
    private static int textSize = SizeUtils.dp2px(15);
    private GestureDetector mGestureDetector;
    private Context mContext;
    private int currentImageIndex;
    private DataCache dataCache;
    private Paint textPaint;
    private Rect textBound;
    private int loadingPercent;
    private Paint loadingBgPaint;
    private RectF textBg;

    public ImageView360(Context context) {
        this(context, null);
    }

    public ImageView360(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageView360(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            mGestureDetector.onTouchEvent(event);
        }
        return true;
    }

    private void init() {
        mGestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (dataCache != null && dataCache.isLoadingFinish) {
                    if (distanceX > 0) {
                        changeImage(currentImageIndex - 1);
                    } else if (distanceX < 0) {
                        changeImage(currentImageIndex + 1);
                    }
                } else {
                    CommonUtil.showErrorToast("全景图片加载中，请稍后");
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
        textBound = new Rect();

        loadingBgPaint = new Paint();
        loadingBgPaint.setColor(Color.parseColor("#BB2E2E2E"));
        loadingBgPaint.setAntiAlias(true);
    }

    /**
     * 设置图片地址
     */
    public void setBitmapUrls(String[] urls) {
        if (dataCache != null) {
            dataCache.isDownloadThreadRun = false;
        }
        if (urls != null && urls.length > 0) {
            byte[] bytes = ImageCache.getInstance().get(urls[0]);
            if (bytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                this.setImageBitmap(bitmap);
            } else {
                Glide.with(mContext)
                        .load(urls[0])
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(this);
            }
        } else {
            this.setImageBitmap(null);
        }
        dataCache = new DataCache(mContext, getWidth(), getHeight(), percent -> {
            loadingPercent = percent;
            postInvalidate();
        });
        dataCache.setBitmapUrls(urls);
    }

    private void changeImage(int index) {
        List<byte[]> imageBytes = dataCache.imageBytes;
        if (imageBytes == null || imageBytes.size() == 0) {
            return;
        }
        int length = imageBytes.size();
        if (index < 0) {
            index = length - 1;
        } else if (index > length - 1) {
            index = 0;
        }
        currentImageIndex = index;
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes.get(currentImageIndex), 0, imageBytes.get(currentImageIndex).length);
        this.setImageBitmap(bitmap);
    }

    private int navBgWidth = SizeUtils.dp2px(50);
    private int navBgHeight = SizeUtils.dp2px(30);
    private int navRadius = SizeUtils.dp2px(30);

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (loadingPercent <= 0 || loadingPercent >= 100) {
            return;
        }
        if (textBg == null) {
            int textBgLeft = (width - navBgWidth) >> 1;
            int textBgTop = (height - navBgHeight) >> 1;
            textBg = new RectF(textBgLeft, textBgTop, textBgLeft + navBgWidth, textBgTop + navBgHeight);
        }
        String perStr = loadingPercent + "%";
        // 画圆角矩形
        canvas.drawRoundRect(textBg, navRadius, navRadius, loadingBgPaint);
        //获取文字区域的矩形大小，以便确定文字正中间的位置
        textPaint.getTextBounds(perStr, 0, perStr.length(), textBound);
        //画百分比
        int x = (width - textBound.width()) >> 1;
        int y = (getHeight() + textBound.height()) >> 1;
        canvas.drawText(perStr, x, y, textPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (dataCache != null) {
            dataCache.isDownloadThreadRun = false;
        }
    }

    public interface Callback {
        void onChange(int position);
    }

    public static class DataCache {
        private boolean isDownloadThreadRun;
        private boolean isLoadingFinish;
        private List<byte[]> imageBytes;
        private Context context;
        private int width;
        private int height;
        private Callback callback;

        DataCache(Context context, int width, int height, Callback callback) {
            this.context = context;
            this.width = width;
            this.height = height;
            this.callback = callback;
        }

        void setBitmapUrls(String[] urls) {
            imageBytes = new ArrayList<>();
            isDownloadThreadRun = true;
            isLoadingFinish = false;
            if (urls == null || urls.length == 0) {
                isLoadingFinish = true;
                LogUtils.d("无全景图片");
                return;
            }
            double length = urls.length;
            new Thread(() -> {
                for (int i = 0; i < urls.length; i++) {
                    String url = urls[i];
                    if (!isDownloadThreadRun) {
                        break;
                    }
                    byte[] bytes = ImageCache.getInstance().get(url);
                    if (bytes == null) {
                        try {
                            File file = Glide.with(context)
                                    .downloadOnly()
                                    .load(url)
                                    .skipMemoryCache(true)
                                    .submit(width - padding, height).get();
                            bytes = FileIOUtils.readFile2BytesByStream(file);
                            ImageCache.getInstance().put(url, bytes);
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (bytes != null) {
                        imageBytes.add(bytes);
                    }
                    int loadingPercent = (int) ((i / length) * 100);
                    this.callback.onChange(loadingPercent);
                }
                this.callback.onChange(100);
                isLoadingFinish = true;
            }).start();
        }
    }

}
