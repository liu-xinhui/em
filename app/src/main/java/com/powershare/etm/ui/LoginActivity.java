package com.powershare.etm.ui;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.StringUtils;
import com.powershare.etm.bean.User;
import com.powershare.etm.databinding.ActivityLoginBinding;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.util.UserCache;
import com.powershare.etm.vm.LoginViewModel;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel loginViewModel;

    @Override
    protected View initContentView() {
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onMounted() {
        //登录
        binding.login.setOnClickListener(view -> {
            CharSequence mobile = binding.mobile.getText();
            if (TextUtils.isEmpty(mobile)) {
                CommonUtil.showToast("请输入手机号");
                binding.mobile.requestFocus();
                return;
            }
            if (!RegexUtils.isMobileSimple(mobile)) {
                CommonUtil.showToast("手机号格式不正确");
                binding.mobile.requestFocus();
                return;
            }
            CharSequence code = binding.code.getText();
            if (TextUtils.isEmpty(code)) {
                CommonUtil.showToast("请输入验证码");
                binding.code.requestFocus();
                return;
            }
            if (StringUtils.length(code) < 6) {
                CommonUtil.showToast("验证码长度为6位");
                binding.code.requestFocus();
                return;
            }
            loginViewModel.login(mobile, code).observe(this, new MyObserver<User>() {
                @Override
                public void onStart() {
                    showLoading();
                }

                @Override
                public void onSuccess(User user) {
                    UserCache.save(UserCache.Field.mobile, user.getMobile());
                    gotoMain(0);
                }

                @Override
                public void onFinish() {
                    hideLoading();
                }
            });
        });

        //发送验证码
        loginViewModel.getCountDownLd().observe(LoginActivity.this, second -> {
            if (second == -1) {
                binding.sendCode.setEnabled(true);
                binding.sendCode.setText("获取验证码");
                return;
            }
            binding.sendCode.setText(CommonUtil.format("重新发送({0})", second));
        });
        binding.sendCode.setOnClickListener(view -> {
            CharSequence mobile = binding.mobile.getText();
            if (!RegexUtils.isMobileSimple(mobile)) {
                binding.mobile.setError("手机号格式不正确");
                binding.mobile.requestFocus();
                return;
            }
            binding.sendCode.setEnabled(false);
            loginViewModel.sendCode(mobile).observe(this, new MyObserver<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    loginViewModel.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    super.onError(e);
                    binding.sendCode.setEnabled(true);
                }
            });
        });

        binding.code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6) {
                    KeyboardUtils.hideSoftInput(binding.code);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //跳过
        binding.skip.setOnClickListener(view -> gotoMain(4));
    }

    private void gotoMain(int tabIndex) {
        ActivityUtils.finishAllActivities(true);
        Intent intent = new Intent(this, EmMainActivity.class);
        intent.putExtra("tabIndex", tabIndex);
        startActivity(intent);
    }
}
