package com.powershare.etm.util;

import android.util.LruCache;

import com.blankj.utilcode.util.EncryptUtils;

public class ImageCache {
    // 图片缓存
    private LruCache<String, byte[]> mImageCache;
    private static final ImageCache imageCache = new ImageCache();

    private ImageCache() {
        initImageCache();
    }

    public static ImageCache getInstance() {
        return imageCache;
    }

    private void initImageCache() {
        // 计算可使用的最大内存
        final int maxMemory = (int) Runtime.getRuntime().maxMemory();
        // 取4分之一的可用内存作为缓存
        final int cacheSize = maxMemory / 4;
        mImageCache = new LruCache<String, byte[]>(cacheSize) {

            @Override
            protected int sizeOf(String key, byte[] bitmap) {
                return bitmap.length;
            }
        };
    }

    public void put(String url, byte[] bitmap) {
        mImageCache.put(EncryptUtils.encryptMD5ToString(url), bitmap);
    }

    public byte[] get(String url) {
        return mImageCache.get(EncryptUtils.encryptMD5ToString(url));
    }
}