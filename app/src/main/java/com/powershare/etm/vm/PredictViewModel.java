package com.powershare.etm.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.powershare.etm.bean.ApiResult;
import com.powershare.etm.bean.PredictCharge;
import com.powershare.etm.bean.TripParam;
import com.powershare.etm.http.ApiManager;
import com.powershare.etm.http.ApiService;

public class PredictViewModel extends ViewModel {
    private ApiService apiService = ApiManager.INSTANCE.getService();

    public LiveData<ApiResult<PredictCharge>> tracePredict(TripParam tripParam) {
        return apiService.trackPredict(tripParam);
    }
}