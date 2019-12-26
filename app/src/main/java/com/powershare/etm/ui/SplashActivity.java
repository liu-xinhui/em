package com.powershare.etm.ui;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.powershare.etm.databinding.ActivitySplashBinding;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.util.UserCache;

public class SplashActivity extends BaseActivity {

    @Override
    protected View initContentView() {
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onMounted() {
        if (TextUtils.isEmpty(UserCache.get(UserCache.Field.token))) {
            go(LoginActivity.class);
        } else {
            go(MainActivity.class);
        }
        finish();
    }
}
