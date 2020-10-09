package com.powershare.etm.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.blankj.utilcode.util.LogUtils;
import com.powershare.etm.App;
import com.powershare.etm.bean.ApiResult;
import com.powershare.etm.bean.Charge;
import com.powershare.etm.bean.ChargeWarn;
import com.powershare.etm.bean.SimpleLocation;
import com.powershare.etm.bean.TotalTrip;
import com.powershare.etm.bean.Trip;
import com.powershare.etm.bean.TripParam;
import com.powershare.etm.bean.TripPoint;
import com.powershare.etm.bean.TripReport;
import com.powershare.etm.bean.TripSoc;
import com.powershare.etm.http.ApiManager;
import com.powershare.etm.http.ApiService;
import com.powershare.etm.util.GlobalValue;
import com.powershare.etm.util.MyObserver;

import java.util.List;

public class TrackViewModel extends AndroidViewModel {
    private ApiService apiService = ApiManager.INSTANCE.getService();
    private MediatorLiveData<TripSoc> tripSoc;
    private MediatorLiveData<ChargeWarn> chargeWarn;
    private AMapLocationClient mLocationClient;

    public TrackViewModel(@NonNull Application application) {
        super(application);
    }

    public MediatorLiveData<TripSoc> getTripSoc() {
        return tripSoc = new MediatorLiveData<>();
    }

    public MediatorLiveData<ChargeWarn> getChargeWarn() {
        return chargeWarn = new MediatorLiveData<>();
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

    public LiveData<ApiResult<TripSoc>> charge(TripPoint tripPoint) {
        return apiService.charge(tripPoint);
    }

    public LiveData<ApiResult<Trip>> getLastTrip() {
        return apiService.getLastTrip();
    }

    public LiveData<ApiResult<TotalTrip>> getTotalTrip() {
        return apiService.getTotalTrip();
    }

    public LiveData<ApiResult<List<SimpleLocation>>> traceRadius(double centerLatitude, double centerLongitude, float mapLevel, float radius) {
        return apiService.traceRadius(centerLatitude, centerLongitude, (int) mapLevel, radius);
    }

    public LiveData<ApiResult<Charge>> getCharge(String chargeId) {
        return apiService.getCharge(chargeId);
    }

    public LiveData<ApiResult<TripReport>> getTripReport() {
        return apiService.getTripReport();
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
        option.setInterval(5 * 1000);
        // 地址信息
        option.setNeedAddress(true);
        mLocationClient.setLocationOption(option);
        mLocationClient.setLocationListener(aMapLocation -> {
            if (aMapLocation.getErrorCode() == 0) {
                LogUtils.d("位置上传");
                TripPoint tripPoint = new TripPoint();
                tripPoint.setTimestamp(System.currentTimeMillis());
                tripPoint.setLatitude(aMapLocation.getLatitude());
                tripPoint.setLongitude(aMapLocation.getLongitude());
                tripPoint.setSpeed(aMapLocation.getSpeed());
                tripPoint.setMileage(GlobalValue.getTrackMileage() / 1000.0);
                tripPoint.setAddress(aMapLocation.getAddress());
                tripPoint.setAg(aMapLocation.getBearing());
                apiService.pushTrack(tripPoint).observeForever(new MyObserver<TripSoc>() {
                    @Override
                    public void onSuccess(TripSoc tripSocApiResult) {
                        tripSoc.setValue(tripSocApiResult);
                        TripParam tripParam = GlobalValue.getTripParam();
                        if (tripSocApiResult.getSoc() > 30) {
                            GlobalValue.getTripParam().setWarned(false);
                        }
                        if (tripParam != null
                                && tripSocApiResult.getSoc() <= tripParam.getWarningLevel()
                                && !tripParam.isWarned()) {
                            GlobalValue.getTripParam().setWarned(true);
                            chargeWarn.setValue(new ChargeWarn(tripPoint, tripSocApiResult));
                        }
                    }
                });
            }
        });
        mLocationClient.startLocation();
        App.startKeepAlive();
    }

    //停止追踪
    public void stopAddTrack() {
        GlobalValue.setTracking(false);
        if (null != mLocationClient) {
            LogUtils.d("停止位置上传");
            mLocationClient.stopLocation();
        }
        App.stopKeepAlive();
    }

}