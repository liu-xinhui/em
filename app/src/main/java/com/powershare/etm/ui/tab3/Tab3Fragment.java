package com.powershare.etm.ui.tab3;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Tip;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.powershare.etm.R;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.bean.TripParam;
import com.powershare.etm.bean.TripPoint;
import com.powershare.etm.databinding.FragmentTab3Binding;
import com.powershare.etm.ui.base.BaseFragment;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.util.SearchLocHistoryHelper;
import com.powershare.etm.vm.AMapViewModel;
import com.powershare.etm.vm.CarViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        //电量
        binding.carModelPowerBar.setProgress(100);
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

        List<String> items = new ArrayList<>();
        Map<String, Integer> tempMap = new HashMap<>();
        int index = 0;
        for (int i = -20; i <= 50; i++) {
            items.add(i + "℃");
            tempMap.put(i + "", index);
            index++;
        }
        OptionsPickerView<String> tempOptions = new OptionsPickerBuilder(activity, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {
                binding.tempValue.setText(items.get(options1).replace("℃", ""));
            }
        }).build();
        tempOptions.setPicker(items);

        View.OnClickListener tempSelect = view -> {
            String currTempStr = binding.tempValue.getText().toString();
            Integer value = tempMap.get(currTempStr);
            if (value != null) {
                tempOptions.setSelectOptions(value);
            }
            tempOptions.show();
        };

        binding.tempSelect.setOnClickListener(tempSelect);
        binding.tempCurrent.setOnClickListener(view -> getTemp());
        //计算路线
        binding.calcRoute.setOnClickListener(view -> {
            Tip endTip = (Tip) binding.recentTrackEndText.getTag();
            if (endTip == null) {
                CommonUtil.showToast("请输入终点");
                return;
            }
            Tip startTip = (Tip) binding.recentTrackStartText.getTag();
            if (startTip == null || binding.recentTrackStartText.getText().equals("我的位置")) {
                binding.calcRoute.showLoading();
                mapViewModel.currentLoc().observe(activity, location -> {
                    binding.calcRoute.hideLoading();
                    LatLonPoint point = new LatLonPoint(location.getLatitude(), location.getLongitude());
                    Tip tip = new Tip();
                    tip.setName(location.getPoiName());
                    tip.setAddress(location.getAddress());
                    tip.setPostion(point);
                    calcRoute(tip, endTip, false);
                });
            } else {
                calcRoute(startTip, endTip, true);
            }
        });
    }

    @Override
    protected void loadData() {
        //车型
        this.getCarListData();
        //温度
        this.getTemp();
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

        OptionsPickerView<CarModel> carOptions = new OptionsPickerBuilder(activity, (options1, option2, options3, v) -> {
            setCurrentCar(mCarModels.get(options1));
        }).build();

        binding.carModelSelect.setOnClickListener(view -> {
            CarModel currentCar = (CarModel) binding.banner.getTag();
            carOptions.setSelectOptions(CarViewModel.findCarIndex(mCarModels, currentCar.getId()));
            carOptions.show();
        });

        carViewModel.carList(false).observe(this, new MyObserver<List<CarModel>>() {
            @Override
            public void onSuccess(List<CarModel> carModels) {
                mCarModels.clear();
                mCarModels.addAll(carModels);
                if (carModels.size() > 0) {
                    setCurrentCar(carModels.get(0));
                }
                carOptions.setPicker(mCarModels);
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
        binding.carModelValue.setText(currentCar.getName());
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

    private void calcRoute(Tip startTip, Tip endTip, boolean save) {
        if (save) {
            SearchLocHistoryHelper.getInstance().addOneHistory(startTip);
        }
        SearchLocHistoryHelper.getInstance().addOneHistory(endTip);
        //车型
        CarModel carModel = (CarModel) binding.banner.getTag();
        if (carModel == null) {
            CommonUtil.showToast("未选择车型");
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
    }
}
