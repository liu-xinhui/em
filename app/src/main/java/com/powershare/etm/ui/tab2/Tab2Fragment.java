package com.powershare.etm.ui.tab2;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.LogUtils;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.databinding.FragmentTab2Binding;
import com.powershare.etm.ui.base.BaseFragment;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.vm.AMapModel;
import com.powershare.etm.vm.CarViewModel;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import java.util.ArrayList;
import java.util.List;

public class Tab2Fragment extends BaseFragment {

    private FragmentTab2Binding binding;
    private Tab2ViewModel tab2ViewModel;
    private CarViewModel carViewModel;
    private AMapModel tempViewModel;

    public static Tab2Fragment newInstance() {
        return new Tab2Fragment();
    }

    @Override
    protected View initContentView(LayoutInflater inflater) {
        binding = FragmentTab2Binding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void createViewModel() {
        tab2ViewModel = ViewModelProviders.of(this).get(Tab2ViewModel.class);
        carViewModel = ViewModelProviders.of(activity).get(CarViewModel.class);
        tempViewModel = ViewModelProviders.of(activity).get(AMapModel.class);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onMounted() {
        //车型
        this.getCarListData();
        //电量
        binding.carModelTempBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        this.getTemp(false);
        binding.tempCurrent.setOnClickListener(view -> getTemp(true));
        View.OnClickListener tempSelect = view -> {
            QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(activity);
            for (int i = -20; i <= 40; i++) {
                builder.addItem(i + "℃");
            }
            builder.setOnSheetItemClickListener((dialog, itemView, position, tag) -> {
                dialog.dismiss();
                binding.tempValue.setText(tag.replace("℃", ""));
            }).build().show();
        };
        binding.tempSelect.setOnClickListener(tempSelect);
    }

    private void getCarListData() {
        List<CarModel> mCarModels = new ArrayList<>();
        QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(activity)
                .setOnSheetItemClickListener((dialog, itemView, position, tag) -> {
                    dialog.dismiss();
                    binding.carModelValue.setText(tag);
                    LogUtils.json(mCarModels.get(position));
                });
        binding.carModelSelect.setOnClickListener(view -> builder.build().show());
        //车辆列表数据
        carViewModel.carList().observe(this, new MyObserver<List<CarModel>>() {
            @Override
            public void onSuccess(List<CarModel> carModels) {
                mCarModels.clear();
                mCarModels.addAll(carModels);
                for (CarModel carModel : carModels) {
                    builder.addItem(carModel.getName());
                }
            }
        });
    }

    private void getTemp(boolean showToast) {
        tempViewModel.temp().observe(this, temp -> {
            binding.tempValue.setText(temp);
            if (showToast) {
                CommonUtil.showSuccessToast("获取温度成功");
            }
        });
    }
}
