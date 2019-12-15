package com.powershare.etm.http;

import android.util.Log;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.google.gson.GsonBuilder;
import com.powershare.etm.util.UserCache;

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
    private String token;

    public static final String BASE_URL = "http://renrentax.com:8080/etm/";

    ApiManager() {
        //开启Log
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(message -> Log.e("TAG", "message=" + message));
        logInterceptor.level(HttpLoggingInterceptor.Level.BODY);
        //增加头部信息
        Interceptor headerInterceptor = chain -> {
            Request.Builder builder = chain.request().newBuilder();
            builder.addHeader("Content-Type", "application/json");
            if (!chain.request().url().uri().getPath().endsWith("/usc/login")) {
                if (StringUtils.isSpace(token)) {
                    token = UserCache.get(UserCache.Field.token);
                }
                if (!StringUtils.isSpace(token)) {
                    builder.addHeader("Cookie", "JSESSIONID=" + token);
                }
            }
            return chain.proceed(builder.build());
        };

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(headerInterceptor)
                .addInterceptor(logInterceptor)
                .cookieJar(new CookieJar() {

                    @Override
                    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                        if (CollectionUtils.isNotEmpty(list)) {
                            Cookie cookie = list.get(0);
                            if ("JSESSIONID".equals(cookie.name())) {
                                token = cookie.value();
                                UserCache.save(UserCache.Field.token, token);
                            }
                        }
                    }

                    @NotNull
                    @Override
                    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                        return new ArrayList<>();
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()))
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .baseUrl(BASE_URL)
                .build();
        this.apiService = retrofit.create(ApiService.class);
    }

    public void clearToken() {
        token = null;
    }

    public ApiService getService() {
        return this.apiService;
    }
}

