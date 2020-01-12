package com.powershare.etm.http;


import androidx.lifecycle.LiveData;

import com.powershare.etm.bean.ApiResult;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.bean.Charge;
import com.powershare.etm.bean.MatchingDegree;
import com.powershare.etm.bean.PredictCharge;
import com.powershare.etm.bean.SimpleLocation;
import com.powershare.etm.bean.TotalTrip;
import com.powershare.etm.bean.Trip;
import com.powershare.etm.bean.TripParam;
import com.powershare.etm.bean.TripPoint;
import com.powershare.etm.bean.TripSoc;
import com.powershare.etm.bean.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    //最近一次行程详情，使用此接口判断是否登录
    @POST("trip/getLastTrip")
    Call<ApiResult<Void>> checkLogin();

    //登录
    @POST("usc/login")
    LiveData<ApiResult<User>> login(@Query("mobile") CharSequence mobile, @Query("code") CharSequence code);

    //发送验证码
    @POST("usc/sendCode")
    LiveData<ApiResult<Void>> sendCode(@Query("mobile") CharSequence mobile);

    //车辆列表
    @POST("carModel/list")
    LiveData<ApiResult<List<CarModel>>> carList();

    //锲合度
    @POST("carModel/getMatchingDegree")
    LiveData<ApiResult<MatchingDegree>> getMatchingDegree(@Query("id") String carId);

    //行程预测
    @POST("trip/predict")
    LiveData<ApiResult<PredictCharge>> trackPredict(@Body TripParam tripParam);

    //行程追踪启动
    @POST("trip/startTrace")
    LiveData<ApiResult<TripSoc>> startTrack(@Body TripParam tripParam);

    //行程追踪结束
    @POST("trip/stopTrace")
    LiveData<ApiResult<Trip>> stopTrack(@Query("discard") CharSequence discard);

    //行程追踪打点
    @POST("trip/trace")
    LiveData<ApiResult<TripSoc>> pushTrack(@Body TripPoint tripPoint);

    //行程追踪充电
    @POST("trip/charge")
    LiveData<ApiResult<TripSoc>> charge(@Body TripPoint tripPoint);

    //行程列表
    @POST("trip/query")
    LiveData<ApiResult<List<Trip>>> traceQuery(@Query("page") int page, @Query("rows") int rows);

    //行程列表
    @POST("trip/get")
    LiveData<ApiResult<Trip>> traceGet(@Query("id") String trackId);

    //获取最近一次行程详情
    @POST("trip/getLastTrip")
    LiveData<ApiResult<Trip>> getLastTrip();

    //获取行程累计信息
    @POST("trip/getTotalTrip")
    LiveData<ApiResult<TotalTrip>> getTotalTrip();

    //获取指定坐标指定半径范围内的所有充电站
    @POST("trip/radius")
    LiveData<ApiResult<List<SimpleLocation>>> traceRadius(@Query("centerLatitude") double centerLatitude, @Query("centerLongitude") double centerLongitude, @Query("mapLevel") int mapLevel, @Query("radius") float radius);

    //充电站详情
    @POST("trip/getLocation")
    LiveData<ApiResult<Charge>> getCharge(@Query("id") String chargeId);
}
