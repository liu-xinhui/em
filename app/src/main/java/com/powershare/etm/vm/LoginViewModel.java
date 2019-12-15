package com.powershare.etm.vm;

import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.powershare.etm.bean.ApiResult;
import com.powershare.etm.bean.User;
import com.powershare.etm.http.ApiManager;
import com.powershare.etm.http.ApiService;
import com.powershare.etm.util.CommonUtil;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends ViewModel {
    private ApiService apiService = ApiManager.INSTANCE.getService();

    @Getter
    private MutableLiveData<Integer> countDownLd = new MutableLiveData<>();
    @Getter
    private static boolean login;

    public LiveData<ApiResult<User>> login(CharSequence mobile, CharSequence code) {
        return apiService.login(mobile, code);
    }

    public LiveData<ApiResult<Void>> sendCode(CharSequence mobile) {
        return apiService.sendCode(mobile);
    }

    /**
     * 倒计时
     */
    public void countDown() {
        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                int second = (int) (millisUntilFinished / 1000);
                countDownLd.setValue(second);
            }

            public void onFinish() {
                countDownLd.setValue(-1);
            }
        }.start();
    }

    public MutableLiveData<Integer> checkLogin() {
        MutableLiveData<Integer> liveData = new MutableLiveData<>();
        apiService.checkLogin().enqueue(new Callback<ApiResult<Void>>() {
            @Override
            public void onResponse(Call<ApiResult<Void>> call, Response<ApiResult<Void>> response) {
                ApiResult<Void> result = response.body();
                if (result != null && result.getStatus() == 200) {
                    liveData.setValue(1);
                    login = true;
                } else {
                    login = false;
                    liveData.setValue(0);
                }
            }

            @Override
            public void onFailure(Call<ApiResult<Void>> call, Throwable t) {
                liveData.setValue(-1);
            }
        });
        return liveData;
    }

    public void checkLoginMain() {
        apiService.checkLogin().enqueue(new Callback<ApiResult<Void>>() {
            @Override
            public void onResponse(Call<ApiResult<Void>> call, Response<ApiResult<Void>> response) {
                ApiResult<Void> result = response.body();
                login = (result == null || result.getStatus() != 403);
            }

            @Override
            public void onFailure(Call<ApiResult<Void>> call, Throwable t) {
                login = true;
            }
        });
    }
}
