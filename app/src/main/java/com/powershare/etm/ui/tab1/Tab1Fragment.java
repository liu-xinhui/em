package com.powershare.etm.ui.tab1;

import android.view.LayoutInflater;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.CollectionUtils;
import com.powershare.etm.R;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.bean.MatchingDegree;
import com.powershare.etm.bean.TotalTrip;
import com.powershare.etm.bean.Trip;
import com.powershare.etm.databinding.FragmentTab1Binding;
import com.powershare.etm.event.StartTrackEvent;
import com.powershare.etm.ui.MainActivity;
import com.powershare.etm.ui.base.BaseFragment;
import com.powershare.etm.ui.tab2.TrackListActivity;
import com.powershare.etm.util.AMapUtil;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.util.PermissionHelper;
import com.powershare.etm.vm.AMapViewModel;
import com.powershare.etm.vm.CarViewModel;
import com.powershare.etm.vm.TrackViewModel;
import com.qmuiteam.qmui.widget.QMUITabSegment;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class Tab1Fragment extends BaseFragment {

    private FragmentTab1Binding binding;
    private CarViewModel carViewModel;
    private TrackViewModel trackViewModel;
    private AMapViewModel mapViewModel;

    private List<CarModel> carModelList;
    private int currentCarIndex;
    private TotalTrip mTotalTrip;

    public static Tab1Fragment newInstance() {
        return new Tab1Fragment();
    }

    @Override
    protected View initContentView(LayoutInflater inflater) {
        binding = FragmentTab1Binding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void createViewModel() {
        trackViewModel = ViewModelProviders.of(this).get(TrackViewModel.class);
        carViewModel = ViewModelProviders.of(activity).get(CarViewModel.class);
        mapViewModel = ViewModelProviders.of(activity).get(AMapViewModel.class);
    }

    @Override
    protected void onMounted() {
        binding.progressCar.setProgressColor(0xBB00F4FF, 0xBB9523C5, 0xBBF44B57, 0xBB9523C5, 0xBB00F4FF);
        binding.progressCar.setShowCircle(false);
        binding.progressCar.showAnimation(100, 500);
        initTabs();
        initProgressTemperature();
        initPreNext();
        binding.startTrack.setOnClickListener(v -> PermissionHelper.getLocPermission(() -> {
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.selectTab(1);
            EventBus.getDefault().post(new StartTrackEvent());
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
                        getMatchingDegree();
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
        binding.recentTrackBg.setOnClickListener(v -> go(TrackListActivity.class));
        binding.countTrackBg.setOnClickListener(v -> go(TrackListActivity.class));
    }

    private void initProgressTemperature() {
        binding.progressTemperature.setMax(70);
        binding.progressTemperature.setProgress(20 + 20);
        binding.progressTemperature.setDraggingEnabled(true);
    }

    private void getCarListData() {
        //车辆列表数据
        carViewModel.carList(false).observe(this, new MyObserver<List<CarModel>>() {

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
                    photoUrls[i] = CommonUtil.getImageUrl(currentCar.getCarModelCode(), photoIds[i]);
                }
                binding.banner.setBitmapUrls(photoUrls);
            } else {
                binding.banner.setBitmapUrls(null);
            }
        }
    }

    @Override
    protected void loadData() {
        getCarListData();
        getTemp();
        trackViewModel.getLastTrip().observe(this, new MyObserver<Trip>() {
            @Override
            public void onSuccess(Trip trip) {
                if (trip != null) {
                    binding.recentTrackStartText.setText(trip.getStartAddress());
                    binding.recentTrackEndText.setText(trip.getDestAddress());
                } else {
                    binding.recentTrackStartText.setText("您最近暂无行程");
                    binding.recentTrackEndText.setText("您最近暂无行程");
                }
            }
        });
        trackViewModel.getTotalTrip().observe(this, new MyObserver<TotalTrip>() {
            @Override
            public void onSuccess(TotalTrip totalTrip) {
                mTotalTrip = totalTrip;
                binding.countTrackNum.setText(String.valueOf(totalTrip.getTotalTimes()));
                binding.mileageBgValue.setText(AMapUtil.formatDouble(totalTrip.getTotalMileage()));
                binding.timeLongBgValue.setText(AMapUtil.formatDouble(totalTrip.getTotalDuration()));
                binding.powerBgValue.setText(AMapUtil.formatDouble(totalTrip.getTotalEnergy()));
                getMatchingDegree();
            }
        });
    }

    private void getTemp() {
        mapViewModel.currentLoc().observe(activity, location -> mapViewModel.temp(location.getCity()).observe(activity, temp -> {
            if (!"none".equals(temp)) {
                binding.progressTemperature.setProgress(20 + Float.parseFloat(temp));
            }
        }));
    }

    private void getMatchingDegree() {
        if (mTotalTrip == null) {
            return;
        }
        if (mTotalTrip.getTotalTimes() >= 2) {
            binding.trackCountNeed.setText("该车与您的匹配度");
            CarModel currentCar = carModelList.get(currentCarIndex);
            carViewModel.getMatchingDegree(currentCar.getId()).observe(Tab1Fragment.this, new MyObserver<MatchingDegree>() {
                @Override
                public void onSuccess(MatchingDegree matchingDegree) {
                    String degree = matchingDegree.getDegree() + "%";
                    binding.trackCount.setText(degree);
                    binding.carFit.setVisibility(View.VISIBLE);
                    binding.carFit.setText(String.format("契合度%s", degree));
                }
            });
        } else {
            binding.trackCount.setText(String.valueOf(mTotalTrip.getTotalTimes()));
        }
    }
}
