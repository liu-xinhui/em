package com.hold.electrify.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

public class MyBannerViewHolder implements com.ms.banner.holder.BannerViewHolder<Object> {

    @Override
    public View createView(Context context, int position, Object data) {
        // 返回mImageView页面布局
        ImageView imageView = new ImageView(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        Glide.with(context).load(data).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(imageView);

        return imageView;
    }

}