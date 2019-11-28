package com.powershare.etm.ui.tab2;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import androidx.lifecycle.ViewModelProviders;

import com.powershare.etm.databinding.FragmentTab2Binding;
import com.powershare.etm.ui.base.BaseFragment;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

public class Tab2Fragment extends BaseFragment {

    private FragmentTab2Binding binding;
    private Tab2ViewModel tab2ViewModel;

    public static Tab2Fragment newInstance() {
        return new Tab2Fragment();
    }

    @Override
    protected View initContentView(LayoutInflater inflater) {
        binding = FragmentTab2Binding.inflate(inflater);
        tab2ViewModel = ViewModelProviders.of(this).get(Tab2ViewModel.class);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onMounted() {
        View.OnClickListener carSelect = view -> new QMUIBottomSheet.BottomListSheetBuilder(activity)
                .addItem("车型1")
                .addItem("车型2")
                .addItem("车型3")
                .addItem("车型4")
                .addItem("车型5")
                .addItem("车型6")
                .addItem("车型7")
                .addItem("车型8")
                .addItem("车型9")
                .addItem("车型10")
                .addItem("车型11")
                .setOnSheetItemClickListener((dialog, itemView, position, tag) -> {
                    dialog.dismiss();
                    binding.carModelValue.setText(tag);
                })
                .build()
                .show();
        binding.carModelSelect.setOnClickListener(carSelect);
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
        /*binding.carModelTempBar.setOnTouchListener((v, me) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });*/
        //温度
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
}
