package com.powershare.etm.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.blankj.utilcode.util.LogUtils;
import com.gyf.cactus.Cactus;
import com.gyf.cactus.callback.CactusCallback;
import com.powershare.etm.bean.ApiResult;
import com.powershare.etm.bean.TripParam;
import com.powershare.etm.http.ApiManager;
import com.powershare.etm.http.ApiService;

public class TraceViewModel extends AndroidViewModel {
    private ApiService apiService = ApiManager.INSTANCE.getService();
    private MutableLiveData<String> trackLiveData;
    private AMapLocationClient mLocationClient;

    public TraceViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<String> trace() {
        if (trackLiveData == null) {
            trackLiveData = new MutableLiveData<>();
        }
        return trackLiveData;
    }

    public LiveData<ApiResult<Object>> startTrace(TripParam tripParam) {
        LogUtils.d(tripParam);
        return apiService.startTrace(tripParam);
    }

    //开始追踪
    public void startAddTrace() {
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
    public void stopAddTrace() {
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
            apiService.pushTrace();
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