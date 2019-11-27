package com.hold.electrify.ui.login;

import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hold.electrify.bean.User;
import com.hold.electrify.databinding.ActivityLoginBinding;
import com.hold.electrify.ui.MainActivity;
import com.hold.electrify.ui.base.BaseActivity;
import com.hold.electrify.util.CommonUtil;
import com.hold.electrify.util.MyObserver;
import com.hold.electrify.util.UserCache;

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
            if (!RegexUtils.isMobileSimple(mobile)) {
                binding.mobile.setError("手机号格式不正确");
                binding.mobile.requestFocus();
                return;
            }
            CharSequence code = binding.code.getText();
            if (StringUtils.length(code) < 6) {
                binding.code.setError("验证码长度6位");
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
                    UserCache.save(UserCache.Field.token, user.getToken());
                    go(MainActivity.class);
                    finish();
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
                    CommonUtil.showSuccessToast("发送成功");
                    loginViewModel.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    super.onError(e);
                    binding.sendCode.setEnabled(true);
                }
            });
        });

        //跳过
        binding.skip.setOnClickListener(view -> {
            go(MainActivity.class);
        });
    }

}
