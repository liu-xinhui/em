package com.powershare.etm.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.LogUtils;
import com.powershare.etm.bean.ApiResult;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.bean.MatchingDegree;
import com.powershare.etm.http.ApiManager;
import com.powershare.etm.http.ApiService;
import com.powershare.etm.util.DataCache;
import com.powershare.etm.util.MyObserver;

import java.util.List;

public class CarViewModel extends ViewModel {
    private ApiService apiService = ApiManager.INSTANCE.getService();

    public LiveData<ApiResult<List<CarModel>>> carList(boolean network) {
        List<CarModel> carModels = DataCache.INSTANCE.getCarModels();
        if (network || CollectionUtils.isEmpty(carModels)) {
            LiveData<ApiResult<List<CarModel>>> liveData = apiService.carList();
            liveData.observeForever(new MyObserver<List<CarModel>>() {
                @Override
                public void onSuccess(List<CarModel> result) {
                    LogUtils.d("---------------------------------" + result.size());
                    DataCache.INSTANCE.setCarModels(result);
                }
            });
            return liveData;
        } else {
            MutableLiveData<ApiResult<List<CarModel>>> liveData = new MutableLiveData<>();
            liveData.setValue(ApiResult.success(carModels));
            return liveData;
        }
    }

    public LiveData<ApiResult<MatchingDegree>> getMatchingDegree(String carId) {
        return apiService.getMatchingDegree(carId);
    }
}