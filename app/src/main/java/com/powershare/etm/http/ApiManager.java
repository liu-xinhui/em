package com.powershare.etm.http;

import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public enum ApiManager {
    INSTANCE;
    private ApiService apiService;

    public static final String BASE_URL = "http://renrentax.com:8080/etm/";

    ApiManager() {
        //开启Log
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(message -> Log.e("TAG", "message=" + message));
        logInterceptor.level(HttpLoggingInterceptor.Level.BODY);
        //增加头部信息
        Interceptor headerInterceptor = chain -> {
            Request.Builder builder = chain.request().newBuilder();
            builder.addHeader("Content-Type", "application/json");
            builder.addHeader("Cookie", "JSESSIONID=D4967DC8D07714A629764647AA1BEE12");
            return chain.proceed(builder.build());
        };

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(headerInterceptor)
                .addInterceptor(logInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()))
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .baseUrl(BASE_URL)
                .build();
        this.apiService = retrofit.create(ApiService.class);
    }

    public ApiService getService() {
        return this.apiService;
    }
}

