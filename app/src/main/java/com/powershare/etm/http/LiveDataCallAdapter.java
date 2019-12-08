package com.powershare.etm.http;


import android.app.Activity;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.powershare.etm.bean.ApiResult;
import com.powershare.etm.ui.login.LoginActivity;
import com.powershare.etm.util.GlobalValue;
import com.powershare.etm.util.UserCache;

import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A Retrofit adapter that converts the Call into a LiveData of ApiResponse.
 */
public class LiveDataCallAdapter<T> implements CallAdapter<ApiResult<T>, LiveData<ApiResult<T>>> {
    private final Type responseType;

    public LiveDataCallAdapter(Type responseType) {
        this.responseType = responseType;
    }

    @Override
    public Type responseType() {
        return responseType;
    }

    @Override
    public LiveData<ApiResult<T>> adapt(Call<ApiResult<T>> call) {
        MutableLiveData<ApiResult<T>> liveData = new MutableLiveData<>();
        liveData.setValue(ApiResult.start());
        call.enqueue(new Callback<ApiResult<T>>() {
            @Override
            public void onResponse(Call<ApiResult<T>> call, Response<ApiResult<T>> response) {
                if (response.isSuccessful()) {
                    ApiResult<T> result = response.body();
                    if (result == null) {
                        liveData.postValue(ApiResult.error("未知错误"));
                        return;
                    }
                    if (result.isApiSuccess()) {
                        liveData.postValue(result.success());
                        return;
                    }
                    if (result.getStatus().equals(403)) {
                        UserCache.clear();
                        Activity topActivity = ActivityUtils.getTopActivity();
                        if (GlobalValue.isNeedGotoLogin() && !(topActivity instanceof LoginActivity)) {
                            topActivity.startActivity(new Intent(topActivity, LoginActivity.class));
                            liveData.postValue(ApiResult.error("请登录"));
                            ActivityUtils.finishAllActivities(true);
                        }
                    } else {
                        liveData.postValue(result.error());
                    }
                } else {
                    liveData.postValue(ApiResult.error("请求失败"));
                }
            }

            @Override
            public void onFailure(Call<ApiResult<T>> call, Throwable throwable) {
                liveData.postValue(ApiResult.error(throwable));
            }
        });
        return liveData;
    }
}
