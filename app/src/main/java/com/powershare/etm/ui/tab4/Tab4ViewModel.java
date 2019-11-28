package com.powershare.etm.ui.tab4;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.powershare.etm.bean.ApiResult;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.http.ApiManager;
import com.powershare.etm.http.ApiService;

import java.util.List;

public class Tab4ViewModel extends ViewModel {
    private ApiService apiService = ApiManager.INSTANCE.getService();

    public LiveData<ApiResult<List<CarModel>>> carList() {
        return apiService.carList();
    }
}