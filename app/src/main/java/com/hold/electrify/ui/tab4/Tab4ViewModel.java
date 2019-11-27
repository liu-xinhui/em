package com.hold.electrify.ui.tab4;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.hold.electrify.bean.ApiResult;
import com.hold.electrify.bean.CarModel;
import com.hold.electrify.http.ApiManager;
import com.hold.electrify.http.ApiService;

import java.util.List;

public class Tab4ViewModel extends ViewModel {
    private ApiService apiService = ApiManager.INSTANCE.getService();

    public LiveData<ApiResult<List<CarModel>>> carList() {
        return apiService.carList();
    }
}