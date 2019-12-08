package com.powershare.etm.ui.tab2;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.FragmentUtils;
import com.powershare.etm.R;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.bean.Trip;
import com.powershare.etm.bean.TripParam;
import com.powershare.etm.bean.TripPoint;
import com.powershare.etm.bean.TripSoc;
import com.powershare.etm.databinding.FragmentStartTrackBinding;
import com.powershare.etm.event.RefreshTrackEvent;
import com.powershare.etm.event.StartTrackEvent;
import com.powershare.etm.ui.base.BaseFragment;
import com.powershare.etm.util.AMapUtil;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.GlobalValue;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.vm.AMapViewModel;
import com.powershare.etm.vm.CarViewModel;
import com.powershare.etm.vm.TrackViewModel;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class StartTrackFragment extends BaseFragment {
    private FragmentStartTrackBinding binding;
    private TrackViewModel trackViewModel;
    private CarViewModel carViewModel;
    private AMapViewModel mapViewModel;

    public static StartTrackFragment newInstance() {
        return new StartTrackFragment();
    }

    @Override
    protected View initContentView(LayoutInflater inflater) {
        binding = FragmentStartTrackBinding.inflate(inflater);
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
        //电量
        binding.carModelPowerBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress < 13) {
                    seekBar.setProgress(0);
                } else if (progress < 38) {
                    seekBar.setProgress(25);
                } else if (progress < 63) {
                    seekBar.setProgress(50);
                } else if (progress < 88) {
                    seekBar.setProgress(75);
                } else {
                    seekBar.setProgress(100);
                }
            }
        });
        //温度
        binding.tempCurrent.setOnClickListener(view -> getTemp());
        View.OnClickListener tempSelect = view -> {
            QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(activity);
            for (int i = -20; i <= 50; i++) {
                builder.addItem(i + "℃");
            }
            builder.setOnSheetItemClickListener((dialog, itemView, position, tag) -> {
                dialog.dismiss();
                binding.tempValue.setText(tag.replace("℃", ""));
            }).build().show();
        };
        binding.tempSelect.setOnClickListener(tempSelect);
        //开启手动追踪
        binding.startTrack.setOnClickListener(view -> startTrack(null));
        binding.recentTrackBg.setOnClickListener(v -> go(TrackListActivity.class));
    }

    void initData() {
        getCarListData();
        getTemp();
        getLastTrack(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getLastTrack(RefreshTrackEvent event) {
        trackViewModel.getLastTrip().observe(this, new MyObserver<Trip>() {
            @Override
            public void onSuccess(Trip trip) {
                if (trip != null) {
                    binding.recentTrackGroup.setVisibility(View.VISIBLE);
                    binding.trackEmptyWrapper.setVisibility(View.GONE);
                    binding.recentTrackStartText.setText(trip.getStartAddress());
                    binding.recentTrackEndText.setText(trip.getDestAddress());
                    binding.mileageBgValue.setText(AMapUtil.formatDouble(trip.getMileage()));
                    binding.powerBgValue.setText(AMapUtil.formatDouble(trip.getStartSoc() - trip.getDestSoc()));
                } else {
                    binding.recentTrackGroup.setVisibility(View.GONE);
                    binding.trackEmptyWrapper.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getCarListData() {
        List<CarModel> mCarModels = new ArrayList<>();
        QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(activity)
                .setOnSheetItemClickListener((dialog, itemView, position, tag) -> {
                    dialog.dismiss();
                    binding.carModelValue.setText(tag);
                    setCurrentCar(mCarModels.get(position));
                });
        binding.carModelSelect.setOnClickListener(view -> builder.build().show());
        //车辆列表数据
        carViewModel.carList(false).observe(this, new MyObserver<List<CarModel>>() {
            @Override
            public void onSuccess(List<CarModel> carModels) {
                mCarModels.clear();
                mCarModels.addAll(carModels);
                if (carModels.size() > 0) {
                    setCurrentCar(carModels.get(0));
                }
                for (CarModel carModel : carModels) {
                    builder.addItem(carModel.getName());
                }
            }
        });
    }

    private void getTemp() {
        binding.tempCurrent.showLoading();
        mapViewModel.currentLoc().observe(activity, location -> mapViewModel.temp(location.getCity()).observe(activity, temp -> {
            binding.tempCurrent.hideLoading();
            if (!"none".equals(temp)) {
                binding.tempValue.setText(temp);
            }
        }));
    }

    private void setCurrentCar(CarModel currentCar) {
        String[] photoIds = currentCar.getPhotoIds();
        binding.banner.setTag(currentCar);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startTrack(StartTrackEvent event) {
        if (GlobalValue.isTracking()) {
            return;
        }
        binding.startTrack.showLoading();
        //车型
        CarModel carModel = (CarModel) binding.banner.getTag();
        if (carModel == null) {
            CommonUtil.showErrorToast("未选择车型");
            return;
        }
        //电量
        int powerProgress = binding.carModelPowerBar.getProgress();
        int power = 10;
        switch (powerProgress) {
            case 0:
                power = 10;
                break;
            case 25:
                power = 15;
                break;
            case 50:
                power = 20;
                break;
            case 75:
                power = 25;
                break;
            case 100:
                power = 30;
                break;
        }
        //温度
        String temp = binding.tempValue.getText().toString();
        TripParam param = new TripParam();
        param.setCarModelId(carModel.getId());
        param.setTemperature(Integer.parseInt(temp));
        param.setWarningLevel(power);
        //当前位置
        mapViewModel.currentLoc().observe(this, aMapLocation -> {
            if (aMapLocation.getErrorCode() != 0) {
                CommonUtil.showErrorToast("定位失败");
                binding.startTrack.hideLoading();
                return;
            }
            TripPoint startPoint = new TripPoint();
            startPoint.setTimestamp(System.currentTimeMillis());
            startPoint.setLatitude(aMapLocation.getLatitude());
            startPoint.setLongitude(aMapLocation.getLongitude());
            startPoint.setSpeed(aMapLocation.getSpeed());
            startPoint.setMileage(0);
            startPoint.setAddress(aMapLocation.getAddress());
            startPoint.setAg(aMapLocation.getBearing());
            param.setStartPoint(startPoint);
            trackViewModel.startTrack(param).observe(StartTrackFragment.this, new MyObserver<TripSoc>() {
                @Override
                public void onSuccess(TripSoc o) {
                    GlobalValue.setTripParam(param);
                    FragmentManager fragmentManager = getFragmentManager();
                    if (fragmentManager != null) {
                        FragmentUtils.add(fragmentManager, TrackingFragment.newInstance(), R.id.fragment_container);
                    }
                }

                @Override
                public void onFinish() {
                    binding.startTrack.hideLoading();
                }
            });
        });
    }

    @Override
    protected boolean useEventBus() {
        return true;
    }
}
