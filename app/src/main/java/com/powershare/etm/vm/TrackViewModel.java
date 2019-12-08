package com.powershare.etm.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AimlessModeListener;
import com.amap.api.navi.enums.AimLessMode;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.blankj.utilcode.util.LogUtils;
import com.powershare.etm.bean.ApiResult;
import com.powershare.etm.bean.TotalTrip;
import com.powershare.etm.bean.Trip;
import com.powershare.etm.bean.TripParam;
import com.powershare.etm.bean.TripPoint;
import com.powershare.etm.bean.TripSoc;
import com.powershare.etm.http.ApiManager;
import com.powershare.etm.http.ApiService;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.GlobalValue;

import java.util.List;

public class TrackViewModel extends AndroidViewModel {
    private ApiService apiService = ApiManager.INSTANCE.getService();
    private MediatorLiveData<ApiResult<TripSoc>> tripSoc;
    private AMapLocationClient mLocationClient;
    private AMapNavi mAMapNavi;

    public TrackViewModel(@NonNull Application application) {
        super(application);
    }

    public MediatorLiveData<ApiResult<TripSoc>> getTripSoc() {
        return tripSoc = new MediatorLiveData<>();
    }

    public LiveData<ApiResult<TripSoc>> startTrack(TripParam tripParam) {
        return apiService.startTrack(tripParam);
    }

    public LiveData<ApiResult<Trip>> stopTrack(CharSequence discard) {
        return apiService.stopTrack(discard);
    }

    public LiveData<ApiResult<List<Trip>>> traceQuery(int pageIndex) {
        return apiService.traceQuery(pageIndex, 10);
    }

    public LiveData<ApiResult<Trip>> traceGet(String trackId) {
        return apiService.traceGet(trackId);
    }

    public LiveData<ApiResult<Trip>> getLastTrip() {
        return apiService.getLastTrip();
    }

    public LiveData<ApiResult<TotalTrip>> getTotalTrip() {
        return apiService.getTotalTrip();
    }

    //开始追踪
    public void startAddTrack() {
        stopAddTrack();
        GlobalValue.setTracking(true);
        mLocationClient = new AMapLocationClient(getApplication());
        AMapLocationClientOption option = new AMapLocationClientOption();
        // 使用连续定位
        option.setOnceLocation(false);
        option.setLocationCacheEnable(false);
        // 每5秒定位一次
        option.setInterval(10 * 1000);
        // 地址信息
        option.setNeedAddress(true);
        mLocationClient.setLocationOption(option);
        mLocationClient.setLocationListener(aMapLocation -> {
            LogUtils.d("位置上传");
            TripPoint tripPoint = new TripPoint();
            tripPoint.setTimestamp(System.currentTimeMillis());
            tripPoint.setLatitude(aMapLocation.getLatitude());
            tripPoint.setLongitude(aMapLocation.getLongitude());
            tripPoint.setSpeed(aMapLocation.getSpeed());
            tripPoint.setMileage(GlobalValue.getTrackMileage() / 1000.0);
            tripPoint.setAddress(aMapLocation.getAddress());
            tripPoint.setAg(aMapLocation.getBearing());
            tripSoc.addSource(apiService.pushTrack(tripPoint), tripSocApiResult -> tripSoc.setValue(tripSocApiResult));
        });
        mLocationClient.startLocation();
        startMapTrack();
    }

    //停止追踪
    public void stopAddTrack() {
        GlobalValue.setTracking(false);
        if (null != mLocationClient) {
            LogUtils.d("停止位置上传");
            mLocationClient.stopLocation();
        }
        if (null != mAMapNavi) {
            LogUtils.d("停止智能巡航");
            mAMapNavi.stopAimlessMode();
        }
    }

    private void startMapTrack() {
        LogUtils.d("开始智能巡航");
        mAMapNavi = AMapNavi.getInstance(getApplication());
        mAMapNavi.startAimlessMode(AimLessMode.NONE_DETECTED);
        mAMapNavi.addAimlessModeListener(new AimlessModeListener() {
            @Override
            public void onUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

            }

            @Override
            public void onUpdateAimlessModeElecCameraInfo(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

            }

            @Override
            public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
                String log = "distance=" + aimLessModeStat.getAimlessModeDistance() + ",time=" + aimLessModeStat.getAimlessModeTime();
                CommonUtil.showSuccessToast(log);
                LogUtils.d(log);
                GlobalValue.setTrackMileage(aimLessModeStat.getAimlessModeDistance());
            }

            @Override
            public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

            }
        });
    }

}