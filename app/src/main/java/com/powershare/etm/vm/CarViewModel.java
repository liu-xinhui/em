package com.powershare.etm.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.blankj.utilcode.util.CollectionUtils;
import com.powershare.etm.bean.ApiResult;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.http.ApiManager;
import com.powershare.etm.http.ApiService;
import com.powershare.etm.util.DataCache;

import java.util.List;

public class CarViewModel extends ViewModel {
    private ApiService apiService = ApiManager.INSTANCE.getService();

    public LiveData<ApiResult<List<CarModel>>> carList(boolean network) {
        List<CarModel> carModels = DataCache.INSTANCE.getCarModels();
        if (network || CollectionUtils.isEmpty(carModels)) {
            LiveData<ApiResult<List<CarModel>>> liveData = apiService.carList();
            ApiResult<List<CarModel>> apiResult = liveData.getValue();
            if (apiResult != null) {
                DataCache.INSTANCE.setCarModels(apiResult.getData());
            }
            return liveData;
        } else {
            MutableLiveData<ApiResult<List<CarModel>>> liveData = new MutableLiveData<>();
            liveData.setValue(ApiResult.success(carModels));
            return liveData;
        }
    }
}