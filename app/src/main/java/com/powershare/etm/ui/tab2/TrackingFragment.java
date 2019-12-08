package com.powershare.etm.ui.tab2;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProviders;

import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.powershare.etm.R;
import com.powershare.etm.bean.Trip;
import com.powershare.etm.bean.TripSoc;
import com.powershare.etm.component.MyDialog;
import com.powershare.etm.databinding.FragmentTrackingBinding;
import com.powershare.etm.ui.base.BaseFragment;
import com.powershare.etm.util.GlobalValue;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.vm.TrackViewModel;

import java.util.ArrayList;
import java.util.List;

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
        binding.progress.setProgress(0);
        binding.cancelTrack.setOnClickListener(view -> new MyDialog.Builder(activity)
                .setContent("确定结束追踪并放弃记录吗？")
                .setSureListener(sureBtn -> FragmentUtils.remove(TrackingFragment.this)).create().show());
        binding.finishTrack.setOnClickListener(view -> {
            if (GlobalValue.getTrackMileage() < 1000) {
                new MyDialog.Builder(activity)
                        .setContent("此次行程未满1KM，将不会被记录。是否结束追踪？")
                        .setSureText("结束追踪")
                        .setSureListener(sureBtn -> FragmentUtils.remove(TrackingFragment.this)).create().show();
            } else {
                new MyDialog.Builder(activity)
                        .setContent("是否结束追踪？")
                        .setSureText("结束追踪")
                        .setSureListener(sureBtn -> trackViewModel.stopTrack("false").observe(TrackingFragment.this, new MyObserver<Trip>() {
                            @Override
                            public void onSuccess(Trip o) {
                                trackViewModel.stopAddTrack();
                                binding.cancelTrack.setVisibility(View.GONE);
                                binding.finishTrack.setVisibility(View.GONE);
                                binding.goDetail.setVisibility(View.VISIBLE);
                                LogUtils.d(o.getId());
                            }
                        })).create().show();
            }
        });
        initGrid(new TripSoc());
        trackViewModel.getTripSoc().observe(this, new MyObserver<TripSoc>() {
            @Override
            public void onSuccess(TripSoc tripSoc) {
                binding.progress.setProgress(tripSoc.getSoc());
                LogUtils.d("update", tripSoc.getSoc());
                initGrid(tripSoc);
            }
        });
        trackViewModel.startAddTrack();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        trackViewModel.stopAddTrack();
    }

    private void initGrid(TripSoc tripSoc) {
        List<String> items = new ArrayList<>();
        items.add(GlobalValue.getTrackMileageKm() + ",km,总里程");
        items.add(tripSoc.getEnergy() + ",kwh,消耗电量");
        items.add(tripSoc.getRmbPublich() + ",RMB,充电成本（公共充电）");
        items.add(tripSoc.getRmbPrivate() + ",RMB,充电成本（私人充电）");
        binding.infoContainer.removeAllViews();
        for (String item : items) {
            String[] itemArr = item.split(",");
            View view = LayoutInflater.from(activity).inflate(R.layout.item_title_value, null);
            TextView value = view.findViewById(R.id.item_title_value);
            TextView unit = view.findViewById(R.id.item_title_value_unit);
            TextView title = view.findViewById(R.id.item_title);
            value.setText(itemArr[0]);
            unit.setText(itemArr[1]);
            title.setText(itemArr[2]);
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1);
            param.width = 0;
            param.height = SizeUtils.dp2px(66);
            int margin = SizeUtils.dp2px(8);
            param.setMargins(margin, margin, margin, margin);
            binding.infoContainer.addView(view, param);
        }
    }
}
