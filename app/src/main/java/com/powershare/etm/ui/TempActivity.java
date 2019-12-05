package com.powershare.etm.ui;

import android.view.View;

import com.powershare.etm.databinding.ActivityTempBinding;
import com.powershare.etm.ui.base.BaseActivity;

public class TempActivity extends BaseActivity {
    private ActivityTempBinding binding;

    @Override
    protected View initContentView() {
        binding = ActivityTempBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onMounted() {
        binding.progressCar.setProgressColor(0xBBF44B57, 0xBBF44B57, 0xBB00F4FF, 0xBB00F4FF, 0xBB9523C5, 0xBB9523C5, 0xBBF44B57);
        binding.progressCar.showAnimation(50, 1500);
    }
}
