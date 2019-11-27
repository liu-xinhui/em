package com.hold.electrify.ui.setting;

import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.hold.electrify.component.MyDialog;
import com.hold.electrify.databinding.ActivitySettingBinding;
import com.hold.electrify.ui.base.BaseActivity;
import com.hold.electrify.ui.login.LoginActivity;
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
        binding.logout.setOnClickListener(view -> {
            new MyDialog.Builder(SettingActivity.this)
                    .setContent("确定退出登录？")
                    .setSureListener(sureBtn -> {
                        go(LoginActivity.class);
                        ActivityUtils.finishAllActivities(true);
                    }).create().show();
        });
    }

    private void initTopBar() {
        mTopBar.setTitle("设置");
        mTopBar.setBackgroundAlpha(1);
        mTopBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

}
