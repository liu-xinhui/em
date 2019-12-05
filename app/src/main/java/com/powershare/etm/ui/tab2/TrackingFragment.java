package com.powershare.etm.ui.tab2;

import android.view.LayoutInflater;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.FragmentUtils;
import com.powershare.etm.component.MyDialog;
import com.powershare.etm.databinding.FragmentTrackingBinding;
import com.powershare.etm.ui.base.BaseFragment;
import com.powershare.etm.util.GlobalValue;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.vm.TrackViewModel;

public class TrackingFragment extends BaseFragment {

    private FragmentTrackingBinding binding;
    private TrackViewModel trackViewModel;

    public static TrackingFragment newInstance() {
        return new TrackingFragment();
    }

    @Override
    protected View initContentView(LayoutInflater inflater) {
        binding = FragmentTrackingBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void createViewModel() {
        trackViewModel = ViewModelProviders.of(this).get(TrackViewModel.class);
    }

    @Override
    protected void onMounted() {
        binding.progress.setProgressColor(0xBBF44B57, 0xBBF44B57, 0xBB00F4FF, 0xBB00F4FF, 0xBB9523C5, 0xBB9523C5, 0xBBF44B57);
        binding.progress.showAnimation(0, 1500);
        binding.cancelTrack.setOnClickListener(view -> new MyDialog.Builder(activity)
                .setContent("确定结束追踪并放弃记录吗？")
                .setSureListener(sureBtn -> {
                    FragmentUtils.remove(TrackingFragment.this);
                    GlobalValue.setTracking(false);
                    trackViewModel.stopAddTrack();
                }).create().show());
        binding.finishTrack.setOnClickListener(view -> {
            if (GlobalValue.getTrackMileage() < 1000) {
                new MyDialog.Builder(activity)
                        .setContent("此次行程未满1KM，将不会被记录。是否结束追踪？")
                        .setSureText("结束追踪")
                        .setSureListener(sureBtn -> {
                            FragmentUtils.remove(TrackingFragment.this);
                            GlobalValue.setTracking(false);
                            trackViewModel.stopAddTrack();
                        }).create().show();
            } else {
                new MyDialog.Builder(activity)
                        .setContent("是否结束追踪？")
                        .setSureText("结束追踪")
                        .setSureListener(sureBtn -> {
                            trackViewModel.stopTrack().observe(TrackingFragment.this, new MyObserver<Object>() {
                                @Override
                                public void onSuccess(Object o) {
                                    GlobalValue.setTracking(false);
                                    trackViewModel.stopAddTrack();
                                    binding.cancelTrack.setVisibility(View.GONE);
                                    binding.finishTrack.setVisibility(View.GONE);
                                    binding.goDetail.setVisibility(View.VISIBLE);
                                }
                            });
                        }).create().show();
            }
        });
        trackViewModel.startAddTrack();
    }
}
