package com.powershare.etm.ui.tab3;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.amap.api.services.help.Tip;
import com.powershare.etm.R;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.bean.TripParam;
import com.powershare.etm.bean.TripPoint;
import com.powershare.etm.databinding.FragmentTab3Binding;
import com.powershare.etm.ui.base.BaseFragment;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.vm.AMapViewModel;
import com.powershare.etm.vm.CarViewModel;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import java.util.ArrayList;
import java.util.List;

public class Tab3Fragment extends BaseFragment {

    private FragmentTab3Binding binding;
    private CarViewModel carViewModel;
    private AMapViewModel mapViewModel;

    public static Tab3Fragment newInstance() {
        return new Tab3Fragment();
    }

    @Override
    protected View initContentView(LayoutInflater inflater) {
        binding = FragmentTab3Binding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void createViewModel() {
        carViewModel = ViewModelProviders.of(activity).get(CarViewModel.class);
        mapViewModel = ViewModelProviders.of(activity).get(AMapViewModel.class);
    }

    @Override
    protected void onMounted() {
        View.OnClickListener onClickListener = view -> {
            Intent intent = new Intent(activity, SearchLocActivity.class);
            intent.putExtra("type", view.getId() == R.id.recent_track_start_text ? 1 : 2);
            startActivityForResult(intent, 1);
        };
        binding.recentTrackStartText.setOnClickListener(onClickListener);
        binding.recentTrackEndText.setOnClickListener(onClickListener);
        //车型
        this.getCarListData();
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
        this.getTemp();
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
        //计算路线
        binding.calcRoute.setOnClickListener(view -> {
            Tip startTip = (Tip) binding.recentTrackStartText.getTag();
            if (startTip == null) {
                CommonUtil.showErrorToast("请输入起点");
                return;
            }
            Tip endTip = (Tip) binding.recentTrackEndText.getTag();
            if (endTip == null) {
                CommonUtil.showErrorToast("请输入终点");
                return;
            }
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
            //构造参数
            TripParam param = new TripParam();
            param.setCarModelId(carModel.getId());
            param.setTemperature(Integer.parseInt(temp));
            param.setWarningLevel(power);

            TripPoint startPoint = new TripPoint();
            startPoint.setLatitude(startTip.getPoint().getLatitude());
            startPoint.setLongitude(startTip.getPoint().getLongitude());
            param.setStartPoint(startPoint);

            TripPoint endPoint = new TripPoint();
            endPoint.setLatitude(endTip.getPoint().getLatitude());
            endPoint.setLongitude(endTip.getPoint().getLongitude());
            param.setDestPoint(endPoint);

            Intent intent = new Intent(activity, PredictActivity.class);
            intent.putExtra("tripParam", param);
            intent.putExtra("startTip", startTip);
            intent.putExtra("endTip", endTip);
            startActivity(intent);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1 && data != null) {
            int type = data.getIntExtra("type", 1);
            Tip item = data.getParcelableExtra("result");
            if (item != null && type == 1) {
                binding.recentTrackStartText.setText(item.getName());
                binding.recentTrackStartText.setTag(item);
            } else if (item != null && type == 2) {
                binding.recentTrackEndText.setText(item.getName());
                binding.recentTrackEndText.setTag(item);
            }
        }
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
}
