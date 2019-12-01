package com.powershare.etm.http;


import androidx.lifecycle.LiveData;

import com.powershare.etm.bean.ApiResult;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.bean.PredictCharge;
import com.powershare.etm.bean.TripParam;
import com.powershare.etm.bean.User;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    //登录
    @POST("usc/login")
    LiveData<ApiResult<User>> login(@Query("mobile") CharSequence mobile, @Query("code") CharSequence code);

    //发送验证码
    @POST("usc/sendCode")
    LiveData<ApiResult<Void>> sendCode(@Query("mobile") CharSequence mobile);

    //车辆列表
    @POST("carModel/list")
    LiveData<ApiResult<List<CarModel>>> carList();

    //行程预测
    @POST("trip/predict")
    LiveData<ApiResult<PredictCharge>> tracePredict(@Body TripParam tripParam);

    //行程追踪启动
    @POST("trip/startTrace")
    LiveData<ApiResult<List<CarModel>>> startTrace();

    //行程追踪结束
    @POST("trip/stopTrace")
    LiveData<ApiResult<List<CarModel>>> stopTrace();

    //行程追踪打点
    @POST("trip/trace")
    LiveData<ApiResult<List<CarModel>>> pushTrace();

    //行程列表
    @POST("trip/query")
    LiveData<ApiResult<List<CarModel>>> traceQuery();


}
