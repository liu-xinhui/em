package com.powershare.etm.ui;

import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.powershare.etm.bean.CarModel;
import com.powershare.etm.component.FrameAnimation2;
import com.powershare.etm.databinding.ActivityTempBinding;
import com.powershare.etm.http.ApiManager;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.ui.tab4.Tab4ViewModel;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.MyObserver;

import java.util.List;

public class TempActivity extends BaseActivity {
    private ActivityTempBinding binding;
    private Tab4ViewModel tab4ViewModel;

    @Override
    protected View initContentView() {
        binding = ActivityTempBinding.inflate(getLayoutInflater());
        tab4ViewModel = ViewModelProviders.of(this).get(Tab4ViewModel.class);
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
        //车辆列表数据
        tab4ViewModel.carList().observe(this, new MyObserver<List<CarModel>>() {
            @Override
            public void onSuccess(List<CarModel> carModels) {
                CarModel currentCar = carModels.get(0);
                String[] photoIds = currentCar.getPhotoIds();
                String[] photoUrls = new String[photoIds.length];
                for (int i = 0; i < photoIds.length; i++) {
                    String photoId = photoIds[i];
                    photoUrls[i] = CommonUtil.format("{0}carModel/photo?carModelCode={1}&photoId={2}", ApiManager.BASE_URL, currentCar.getCarModelCode(), photoId);
                }
                binding.surfaceView.setBitmapUrls(photoUrls);
                //binding.surfaceView.start();
            }
        });
    }

}
