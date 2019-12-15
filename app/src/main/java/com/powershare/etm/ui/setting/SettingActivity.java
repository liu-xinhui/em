package com.powershare.etm.ui.setting;

import android.text.TextUtils;
import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.powershare.etm.component.MyDialog;
import com.powershare.etm.databinding.ActivitySettingBinding;
import com.powershare.etm.http.ApiManager;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.ui.LoginActivity;
import com.powershare.etm.util.UserCache;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

public class SettingActivity extends BaseActivity {
    private ActivitySettingBinding binding;
    private QMUITopBarLayout mTopBar;

    @Override
    protected View initContentView() {
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        mTopBar = binding.topBar;
        return binding.getRoot();
    }

    @Override
    protected void onMounted() {
        initTopBar();
        String phone = UserCache.get(UserCache.Field.mobile);
        boolean isLogin = !TextUtils.isEmpty(phone);
        binding.logout.setText(isLogin ? "退出登录" : "登录");
        binding.logout.setOnClickListener(view -> {
            if (isLogin) {
                new MyDialog.Builder(SettingActivity.this)
                        .setContent("确定退出登录？")
                        .setSureListener(sureBtn -> {
                            UserCache.clear();
                            ApiManager.INSTANCE.clearToken();
                            go(LoginActivity.class);
                            ActivityUtils.finishAllActivities(true);
                        }).create().show();
            } else {
                go(LoginActivity.class);
            }
        });
        binding.phone.setText(isLogin ? phone : "未登录");
    }

    private void initTopBar() {
        mTopBar.setTitle("设置");
        mTopBar.setBackgroundAlpha(1);
        mTopBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

}
