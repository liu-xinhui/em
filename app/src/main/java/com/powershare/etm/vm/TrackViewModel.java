package com.powershare.etm.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.navi.AMapNavi;
import com.amap.api.track.AMapTrackClient;
import com.blankj.utilcode.util.LogUtils;
import com.gyf.cactus.Cactus;
import com.gyf.cactus.callback.CactusCallback;
import com.powershare.etm.bean.ApiResult;
import com.powershare.etm.bean.TripParam;
import com.powershare.etm.bean.TripPoint;
import com.powershare.etm.bean.TripSoc;
import com.powershare.etm.http.ApiManager;
import com.powershare.etm.http.ApiService;
import com.powershare.etm.util.GlobalValue;

public class TrackViewModel extends AndroidViewModel {
    private ApiService apiService = ApiManager.INSTANCE.getService();
    private static MediatorLiveData<ApiResult<TripSoc>> tripSoc;
    private AMapLocationClient mLocationClient;
    private static boolean isServiceOpen;

    public TrackViewModel(@NonNull Application application) {
        super(application);
    }

    public MediatorLiveData<ApiResult<TripSoc>> getTripSoc() {
        return tripSoc = new MediatorLiveData<>();
    }

    public LiveData<ApiResult<TripSoc>> startTrack(TripParam tripParam) {
        return apiService.startTrack(tripParam);
    }

    public LiveData<ApiResult<String>> stopTrack(CharSequence discard) {
        return apiService.stopTrack(discard);
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
                    public void doWork(int times) {
                        startLocation();
                    }

                    @Override
                    public void onStop() {
                        LogUtils.d("服务关闭");
                        stopLocation();
                    }
                })
                .register(getApplication());
    }

    //停止追踪
    public void stopAddTrack() {
        isServiceOpen = false;
        Cactus.getInstance().unregister(getApplication());
        stopLocation();
    }

    /**
     * 启动定位
     */
    private synchronized void startLocation() {
        if (isServiceOpen) {
            return;
        }
        isServiceOpen = true;
        stopLocation();
        mLocationClient = new AMapLocationClient(getApplication());
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
            LogUtils.d("位置上传");
            GlobalValue.setTrackMileage(10);
            TripPoint tripPoint = new TripPoint();
            tripPoint.setTimestamp(System.currentTimeMillis());
            tripPoint.setLatitude(aMapLocation.getLatitude());
            tripPoint.setLongitude(aMapLocation.getLongitude());
            tripPoint.setSpeed(aMapLocation.getSpeed());
            tripPoint.setMileage(10);
            tripPoint.setAddress(aMapLocation.getAddress());
            tripPoint.setAg(aMapLocation.getBearing());
            tripSoc.addSource(apiService.pushTrack(tripPoint), tripSocApiResult -> tripSoc.setValue(tripSocApiResult));
        });
        mLocationClient.startLocation();
    }

    /**
     * 停止定位
     */
    private void stopLocation() {
        if (null != mLocationClient) {
            LogUtils.d("停止位置上传");
            mLocationClient.stopLocation();
        }
    }

    private void mapTrack() {
        AMapNavi mAMapNavi = AMapNavi.getInstance(getApplication());
        
    }


}