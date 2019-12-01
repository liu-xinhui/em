package com.powershare.etm.ui;

import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.powershare.etm.component.FrameAnimation2;
import com.powershare.etm.databinding.ActivityTempBinding;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.vm.CarViewModel;

public class TempActivity extends BaseActivity {
    private ActivityTempBinding binding;
    private CarViewModel carViewModel;

    @Override
    protected View initContentView() {
        binding = ActivityTempBinding.inflate(getLayoutInflater());
        carViewModel = ViewModelProviders.of(this).get(CarViewModel.class);
        return binding.getRoot();
    }

    @Override
    protected void onMounted() {
        binding.surfaceView.setOnFrameFinishedListener(new FrameAnimation2.OnFrameFinishedListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onStop() {
                //binding.surfaceView.start();
            }
        });
        getCarListData();
    }

    private void getCarListData() {
    }

}
