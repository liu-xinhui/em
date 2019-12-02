package com.powershare.etm.vm;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.powershare.etm.App;
import com.powershare.etm.util.CommonUtil;

public class AMapModel extends ViewModel {
    private MutableLiveData<String> tempLiveData;
    private AMapLocationClient mLocationClient;

    public MutableLiveData<String> temp() {
        if (tempLiveData == null) {
            tempLiveData = new MutableLiveData<>();
            refreshTemp();
        }
        return tempLiveData;
    }

    public void refreshTemp() {
        getCurrentLoc(loc -> {
            String city;
            if (loc.getErrorCode() == 0) {
                city = loc.getCity();
            } else {
                //CommonUtil.showErrorToast("定位失败，自动获取上海市温度");
                city = "上海市";
            }
            WeatherSearchQuery query = new WeatherSearchQuery(city, WeatherSearchQuery.WEATHER_TYPE_LIVE);
            WeatherSearch weatherSearch = new WeatherSearch(App.getInstance());
            weatherSearch.setOnWeatherSearchListener(new WeatherSearch.OnWeatherSearchListener() {
                @Override
                public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int i) {
                    tempLiveData.setValue(localWeatherLiveResult.getLiveResult().getTemperature());
                }

                @Override
                public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

                }
            });
            weatherSearch.setQuery(query);
            weatherSearch.searchWeatherAsyn();
        });

    }

    public void getCurrentLoc(AMapLocationListener listener) {
        //初始化定位
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(App.getInstance());
            //设置定位回调监听
            mLocationClient.setLocationListener(listener);
            //声明AMapLocationClientOption对象
            AMapLocationClientOption option = new AMapLocationClientOption();
            //设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
            option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
            mLocationClient.setLocationOption(option);
        }
        if (null != mLocationClient) {
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient.stopLocation();
            mLocationClient.startLocation();
        }
    }
}