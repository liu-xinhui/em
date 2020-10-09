package com.powershare.etm.ui;

import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.powershare.etm.bean.User;
import com.powershare.etm.databinding.ActivitySplashBinding;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.util.UserCache;
import com.powershare.etm.vm.LoginViewModel;

public class SplashActivity extends BaseActivity {

    private LoginViewModel loginViewModel;

    @Override
    protected View initContentView() {
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onMounted() {
        loginViewModel.login("18112702002", "735820").observe(this, new MyObserver<User>() {
            @Override
            public void onStart() {
                showLoading();
            }

            @Override
            public void onSuccess(User user) {
                UserCache.save(UserCache.Field.mobile, user.getMobile());
                go(EmMainActivity.class);
                finish();
            }

            @Override
            public void onFinish() {
                hideLoading();
            }
        });
    }
}
