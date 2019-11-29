package com.powershare.etm.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.powershare.etm.bean.ApiResult;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.http.ApiManager;
import com.powershare.etm.http.ApiService;

import java.util.List;

public class CarViewModel extends ViewModel {
    private ApiService apiService = ApiManager.INSTANCE.getService();
    private MediatorLiveData<ApiResult<List<CarModel>>> carListLiveData = new MediatorLiveData<>();

    public void refreshCarList() {
        carListLiveData.addSource(apiService.carList(), value -> carListLiveData.setValue(value));
    }

    public LiveData<ApiResult<List<CarModel>>> carList() {
        return carListLiveData;
    }

}