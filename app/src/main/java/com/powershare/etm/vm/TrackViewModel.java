package com.powershare.etm.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.blankj.utilcode.util.LogUtils;
import com.gyf.cactus.Cactus;
import com.gyf.cactus.callback.CactusCallback;
import com.powershare.etm.bean.ApiResult;
import com.powershare.etm.bean.TripParam;
import com.powershare.etm.bean.TripPoint;
import com.powershare.etm.http.ApiManager;
import com.powershare.etm.http.ApiService;
import com.powershare.etm.util.GlobalValue;
import com.powershare.etm.util.MyObserver;

public class TrackViewModel extends AndroidViewModel {
    private ApiService apiService = ApiManager.INSTANCE.getService();
    private AMapLocationClient mLocationClient;

    public TrackViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<ApiResult<Object>> startTrack(TripParam tripParam) {
        return apiService.startTrack(tripParam);
    }

    public LiveData<ApiResult<Object>> stopTrack() {
        return apiService.stopTrack();
    }

    //开始追踪
    public void startAddTrack() {
        GlobalValue.setTracking(true);
        Cactus.getInstance()
                .isDebug(true)
                .hideNotification(false)
                .hideNotificationAfterO(false)
                .addCallback(new CactusCallback() {
                    @Override
                    public void doWork(int i) {
                        //高德地图上传位置
                        startLocation();
                    }

                    @Override
                    public void onStop() {
                        stopLocation();
                    }
                })
                .register(getApplication());
    }

    //停止追踪
    public void stopAddTrack() {
        Cactus.getInstance().unregister(getApplication());
        stopLocation();
    }

    /**
     * 启动定位
     */
    private void startLocation() {
        stopLocation();
        if (null == mLocationClient) {
            mLocationClient = new AMapLocationClient(getApplication());
        }
        AMapLocationClientOption option = new AMapLocationClientOption();
        // 使用连续定位
        option.setOnceLocation(false);
        option.setLocationCacheEnable(false);
        // 每5秒定位一次
        option.setInterval(5 * 1000);
        // 地址信息
        option.setNeedAddress(true);
        mLocationClient.setLocationOption(option);
        mLocationClient.setLocationListener(aMapLocation -> {
            GlobalValue.setTrackMileage(10);
            TripPoint tripPoint = new TripPoint();
            tripPoint.setTimestamp(System.currentTimeMillis());
            tripPoint.setLatitude(aMapLocation.getLatitude());
            tripPoint.setLongitude(aMapLocation.getLongitude());
            tripPoint.setSpeed(aMapLocation.getSpeed());
            tripPoint.setMileage(10);
            tripPoint.setAddress(aMapLocation.getAddress());
            tripPoint.setAg(aMapLocation.getBearing());
            apiService.pushTrack(tripPoint).observeForever(new MyObserver<Object>() {
                @Override
                public void onSuccess(Object o) {

                }
            });
        });
        mLocationClient.startLocation();
    }

    /**
     * 停止定位
     */
    private void stopLocation() {
        if (null != mLocationClient) {
            mLocationClient.stopLocation();
        }
    }
}