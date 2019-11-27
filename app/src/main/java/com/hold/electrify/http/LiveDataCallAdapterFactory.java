package com.hold.electrify.http;

import androidx.lifecycle.LiveData;

import com.hold.electrify.bean.ApiResult;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;

public class LiveDataCallAdapterFactory extends CallAdapter.Factory {

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != LiveData.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalStateException("Response must be parametrized as LiveData<Result>");
        }
        Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
        Class<?> rawResponseType = getRawType(responseType);
        if (rawResponseType != ApiResult.class) {
            throw new IllegalArgumentException("Response must be parametrized as LiveData<Result>");
        }
        return new LiveDataCallAdapter<>(responseType);
    }
}
