package com.hold.electrify.ui.tab1;

import android.view.LayoutInflater;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.CollectionUtils;
import com.hold.electrify.R;
import com.hold.electrify.bean.CarModel;
import com.hold.electrify.databinding.FragmentTab1Binding;
import com.hold.electrify.http.ApiManager;
import com.hold.electrify.ui.base.BaseFragment;
import com.hold.electrify.ui.tab4.Tab4ViewModel;
import com.hold.electrify.util.CommonUtil;
import com.hold.electrify.util.MyObserver;
import com.hold.electrify.util.PermissionHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;

import java.util.List;

public class Tab1Fragment extends BaseFragment {

    private FragmentTab1Binding binding;
    private Tab1ViewModel tab1ViewModel;
    private Tab4ViewModel tab4ViewModel;

    private List<CarModel> carModelList;
    private int currentCarIndex;

    public static Tab1Fragment newInstance() {
        return new Tab1Fragment();
    }

    @Override
    protected View initContentView(LayoutInflater inflater) {
        binding = FragmentTab1Binding.inflate(inflater);
        tab1ViewModel = ViewModelProviders.of(this).get(Tab1ViewModel.class);
        tab4ViewModel = ViewModelProviders.of(this).get(Tab4ViewModel.class);
        return binding.getRoot();
    }

    @Override
    protected void onMounted() {
        binding.progressCar.setProgressColor(0xBB00F4FF, 0xBB9523C5, 0xBBF44B57, 0xBB9523C5, 0xBB00F4FF);
        binding.progressCar.setShowCircle(false);
        binding.progressCar.showAnimation(100, 500);
        initTabs();
        initProgressTemperature();
        getCarListData();
        initPreNext();
        binding.startTrack.setOnClickListener(v -> PermissionHelper.getLocPermission(() -> {

        }));
    }

    private void initTabs() {
        binding.tabs.setDefaultNormalColor(ContextCompat.getColor(activity, R.color.grayA6));
        binding.tabs.setDefaultSelectedColor(ContextCompat.getColor(activity, R.color.colorAccent));
        QMUITabSegment.Tab tab1 = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(activity, R.mipmap.tab_car),
                ContextCompat.getDrawable(activity, R.mipmap.tab_car_active),
                getText(R.string.car_model), false
        );
        QMUITabSegment.Tab tab2 = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(activity, R.mipmap.tab_track),
                ContextCompat.getDrawable(activity, R.mipmap.tab_track_active),
                getText(R.string.track), false
        );
        QMUITabSegment.Tab tab3 = new QMUITabSegment.Tab(
                ContextCompat.getDrawable(activity, R.mipmap.tab_temperature),
                ContextCompat.getDrawable(activity, R.mipmap.tab_temperature_active),
                getText(R.string.temperature), false
        );
        binding.tabs.addTab(tab1)
                .addTab(tab2)
                .addTab(tab3);
        binding.tabs.selectTab(0);
        binding.tabs.notifyDataChanged();
        binding.tabs.addOnTabSelectedListener(new QMUITabSegment.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int index) {
                switch (index) {
                    case 0:
                        binding.progressCar.setVisibility(View.INVISIBLE);
                        binding.banner.setVisibility(View.VISIBLE);
                        binding.progressTemperature.setVisibility(View.INVISIBLE);
                        binding.trackCount.setVisibility(View.INVISIBLE);
                        binding.trackCountNeed.setVisibility(View.INVISIBLE);
                        binding.pre.setVisibility(View.VISIBLE);
                        binding.next.setVisibility(View.VISIBLE);
                        binding.bannerNav.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        binding.progressCar.setVisibility(View.VISIBLE);
                        binding.banner.setVisibility(View.INVISIBLE);
                        binding.progressTemperature.setVisibility(View.INVISIBLE);
                        binding.trackCount.setVisibility(View.VISIBLE);
                        binding.trackCountNeed.setVisibility(View.VISIBLE);
                        binding.pre.setVisibility(View.GONE);
                        binding.next.setVisibility(View.GONE);
                        binding.bannerNav.setVisibility(View.GONE);
                        break;
                    case 2:
                        binding.progressCar.setVisibility(View.INVISIBLE);
                        binding.banner.setVisibility(View.INVISIBLE);
                        binding.progressTemperature.setVisibility(View.VISIBLE);
                        binding.trackCount.setVisibility(View.INVISIBLE);
                        binding.trackCountNeed.setVisibility(View.INVISIBLE);
                        binding.pre.setVisibility(View.GONE);
                        binding.next.setVisibility(View.GONE);
                        binding.bannerNav.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(int index) {

            }

            @Override
            public void onTabReselected(int index) {

            }

            @Override
            public void onDoubleTap(int index) {

            }
        });
    }

    private void initProgressTemperature() {
        binding.progressTemperature.setMax(60);
        binding.progressTemperature.setProgress(20);
        binding.progressTemperature.setDraggingEnabled(true);
    }

    private void getCarListData() {
        //车辆列表数据
        tab4ViewModel.carList().observe(this, new MyObserver<List<CarModel>>() {

            @Override
            public void onSuccess(List<CarModel> carModels) {
                carModelList = carModels;
                currentCarIndex = 0;
                setCurrentCar();
            }
        });
    }

    private void initPreNext() {
        binding.pre.setOnClickListener(view -> {
            if (currentCarIndex != 0) {
                currentCarIndex = currentCarIndex - 1;
                setCurrentCar();
            }
        });
        binding.next.setOnClickListener(view -> {
            if (carModelList != null && currentCarIndex < carModelList.size() - 1) {
                currentCarIndex = currentCarIndex + 1;
                setCurrentCar();
            }
        });
    }

    private void setCurrentCar() {
        if (CollectionUtils.isNotEmpty(carModelList)) {
            String nav = (currentCarIndex + 1) + "/" + carModelList.size();
            binding.bannerNav.setText(nav);
        }
        if (carModelList != null && carModelList.size() > currentCarIndex) {
            CarModel currentCar = carModelList.get(currentCarIndex);
            binding.carTitle.setText(currentCar.getName());
            String[] photoIds = currentCar.getPhotoIds();
            if (photoIds != null && photoIds.length > 0) {
                String[] photoUrls = new String[photoIds.length];
                for (int i = 0; i < photoIds.length; i++) {
                    photoUrls[i] = CommonUtil.format("{0}carModel/photo?carModelCode={1}&photoId={2}", ApiManager.BASE_URL, currentCar.getCarModelCode(), photoIds[i]);
                }
                binding.banner.setBitmapUrls(photoUrls);
            } else {
                binding.banner.setBitmapUrls(null);
            }
        }
    }
}
