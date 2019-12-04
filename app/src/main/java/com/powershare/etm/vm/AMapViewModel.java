package com.powershare.etm.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.powershare.etm.App;
import com.powershare.etm.util.CommonUtil;

public class AMapViewModel extends ViewModel {
    private AMapLocationClient mLocationClient;

    public LiveData<String> temp(String city) {
        MutableLiveData<String> liveData = new MutableLiveData<>();
        WeatherSearchQuery query = new WeatherSearchQuery(city, WeatherSearchQuery.WEATHER_TYPE_LIVE);
        WeatherSearch weatherSearch = new WeatherSearch(App.getInstance());
        weatherSearch.setOnWeatherSearchListener(new WeatherSearch.OnWeatherSearchListener() {
            @Override
            public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int code) {
                if (code == 1000) {
                    liveData.setValue(localWeatherLiveResult.getLiveResult().getTemperature());
                } else {
                    liveData.setValue("none");
                    CommonUtil.showErrorToast("获取温度失败");
                }
            }

            @Override
            public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

            }
        });
        weatherSearch.setQuery(query);
        weatherSearch.searchWeatherAsyn();
        return liveData;
    }

    public LiveData<AMapLocation> currentLoc() {
        MutableLiveData<AMapLocation> locLiveData = new MutableLiveData<>();
        //初始化定位
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(App.getInstance());
            //设置定位回调监听
            //声明AMapLocationClientOption对象
            AMapLocationClientOption option = new AMapLocationClientOption();
            //设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
            option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
            mLocationClient.setLocationOption(option);
        }
        if (null != mLocationClient) {
            mLocationClient.setLocationListener(locLiveData::setValue);
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient.stopLocation();
            mLocationClient.startLocation();
        }
        return locLiveData;
    }
}