package com.hold.electrify.ui.login;

import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hold.electrify.bean.ApiResult;
import com.hold.electrify.bean.User;
import com.hold.electrify.http.ApiManager;
import com.hold.electrify.http.ApiService;

import lombok.Getter;

public class LoginViewModel extends ViewModel {

    private ApiService apiService = ApiManager.INSTANCE.getService();

    @Getter
    private MutableLiveData<Integer> countDownLd = new MutableLiveData<>();

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
}
