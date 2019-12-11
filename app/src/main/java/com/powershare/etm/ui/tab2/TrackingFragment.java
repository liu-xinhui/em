package com.powershare.etm.ui.tab2;

import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProviders;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AimlessModeListener;
import com.amap.api.navi.enums.AimLessMode;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.powershare.etm.R;
import com.powershare.etm.bean.Trip;
import com.powershare.etm.bean.TripSoc;
import com.powershare.etm.component.MyDialog;
import com.powershare.etm.databinding.FragmentTrackingBinding;
import com.powershare.etm.event.RefreshTrackEvent;
import com.powershare.etm.ui.base.BaseFragment;
import com.powershare.etm.util.GlobalValue;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.vm.TrackViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class TrackingFragment extends BaseFragment {

    private FragmentTrackingBinding binding;
    private TrackViewModel trackViewModel;
    private AMapNavi mAMapNavi;

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
                .setSureListener(sureBtn -> trackViewModel.stopTrack("true").observe(TrackingFragment.this, new MyObserver<Trip>() {
                    @Override
                    public void onSuccess(Trip o) {
                        FragmentUtils.remove(TrackingFragment.this);
                    }
                })).create().show());
        binding.finishTrack.setOnClickListener(view -> new MyDialog.Builder(activity)
                .setContent(GlobalValue.getTrackMileage() < 1000 ? "此次行程未满1KM，是否结束追踪？" : "是否结束追踪？")
                .setSureText("结束追踪")
                .setSureListener(sureBtn -> trackViewModel.stopTrack("false").observe(TrackingFragment.this, new MyObserver<Trip>() {
                    @Override
                    public void onSuccess(Trip o) {
                        EventBus.getDefault().post(new RefreshTrackEvent());
                        trackViewModel.stopAddTrack();
                        binding.cancelTrack.setVisibility(View.GONE);
                        binding.finishTrack.setVisibility(View.GONE);
                        binding.goDetail.setVisibility(View.VISIBLE);
                        binding.notice.setText("追踪结束~");
                        binding.goDetail.setOnClickListener(view -> {
                            Intent intent = new Intent(activity, TrackDetailActivity.class);
                            intent.putExtra("trickId", o.getId());
                            startActivity(intent);
                            new Handler().postDelayed(() -> FragmentUtils.remove(TrackingFragment.this), 1000);
                        });
                    }
                })).create().show());
        initGrid(new TripSoc());
        trackViewModel.getTripSoc().observe(this, tripSoc -> {
            binding.progress.setProgress(tripSoc.getSoc());
            LogUtils.d("update", tripSoc.getSoc());
            initGrid(tripSoc);
        });
        trackViewModel.startAddTrack();
        startMapTrack();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        trackViewModel.stopAddTrack();
        stopMapTrack();
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

    private void startMapTrack() {
        LogUtils.d("开始智能巡航");
        //CommonUtil.showSuccessToast("开始智能巡航");
        mAMapNavi = AMapNavi.getInstance(activity);
        mAMapNavi.setUseInnerVoice(false);
        mAMapNavi.startAimlessMode(AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED);
        mAMapNavi.addAimlessModeListener(new AimlessModeListener() {
            @Override
            public void onUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
            }

            @Override
            public void onUpdateAimlessModeElecCameraInfo(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
            }

            @Override
            public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
                //String log = "distance=" + aimLessModeStat.getAimlessModeDistance() + ",time=" + aimLessModeStat.getAimlessModeTime();
                //CommonUtil.showSuccessToast(log);
                GlobalValue.setTrackMileage(aimLessModeStat.getAimlessModeDistance());
            }

            @Override
            public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {
            }
        });
    }

    private void stopMapTrack() {
        if (null != mAMapNavi) {
            LogUtils.d("停止智能巡航");
            //CommonUtil.showSuccessToast("停止智能巡航");
            mAMapNavi.stopAimlessMode();
        }
    }
}
