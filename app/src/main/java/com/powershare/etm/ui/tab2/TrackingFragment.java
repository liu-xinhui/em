package com.powershare.etm.ui.tab2;

import android.os.CountDownTimer;
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
import com.blankj.utilcode.util.NotificationUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.powershare.etm.R;
import com.powershare.etm.bean.Trip;
import com.powershare.etm.bean.TripPoint;
import com.powershare.etm.bean.TripSoc;
import com.powershare.etm.component.MyDialog;
import com.powershare.etm.databinding.FragmentTrackingBinding;
import com.powershare.etm.event.RefreshTrackEvent;
import com.powershare.etm.event.ToChargeEvent;
import com.powershare.etm.ui.EmMainActivity;
import com.powershare.etm.ui.base.BaseFragment;
import com.powershare.etm.util.AMapUtil;
import com.powershare.etm.util.CommonUtil;
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
        //binding.progress.setProgressColor(0xBBF44B57, 0xBBF44B57, 0xBB00F4FF, 0xBB00F4FF, 0xBB9523C5, 0xBB9523C5, 0xBBF44B57);
        binding.progress.setProgressColor(0xBB00F4FF, 0xBB00F4FF, 0xBBF44B57, 0xBBF44B57, 0xBBF44B57, 0xBB9523C5, 0xBB9523C5, 0xBB00F4FF, 0xBB00F4FF);
        binding.progress.setProgress(0);
        binding.cancelTrack.setOnClickListener(view -> new MyDialog.Builder(activity)
                .setContent("确定结束追踪并放弃记录吗？")
                .setSureListener(sureBtn -> trackViewModel.stopTrack("true").observe(TrackingFragment.this, new MyObserver<Trip>() {
                    @Override
                    public void onSuccess(Trip o) {
                        FragmentUtils.remove(TrackingFragment.this);
                    }
                })).create().show());
        binding.finishTrack.setOnClickListener(view -> {
            boolean isThan1Km = GlobalValue.getTrackMileage() >= 1000;
            new MyDialog.Builder(activity)
                    .setContent(isThan1Km ? "是否结束追踪？" : "此次行程未满1km，将不会被记录。是否结束追踪？")
                    .setSureText("结束追踪")
                    .setSureListener(sureBtn -> trackViewModel.stopTrack(isThan1Km ? "false" : "true").observe(TrackingFragment.this, new MyObserver<Trip>() {
                        @Override
                        public void onSuccess(Trip o) {
                            if (isThan1Km) {
                                CommonUtil.showToast("行程记录成功");
                                EventBus.getDefault().post(new RefreshTrackEvent());
                                trackViewModel.stopAddTrack();
                                binding.cancelTrack.setVisibility(View.GONE);
                                binding.finishTrack.setVisibility(View.GONE);
                                binding.goDetail.setVisibility(View.VISIBLE);
                                binding.notice.setText("追踪结束~");
                                binding.goDetail.setOnClickListener(view -> {
                                    TrackDetailActivity.go(activity, o.getId());
                                    new Handler().postDelayed(() -> FragmentUtils.remove(TrackingFragment.this), 1000);
                                });
                            } else {
                                FragmentUtils.remove(TrackingFragment.this);
                            }
                        }
                    })).create().show();
        });
        initGrid(new TripSoc());
        trackViewModel.getTripSoc().observe(this, tripSoc -> {
            binding.progress.setProgress(tripSoc.getSoc());
            if (tripSoc.getChargeTimes() > 0) {
                binding.notice.setVisibility(View.VISIBLE);
                String notice = "您已完成充电" + tripSoc.getChargeTimes() + "次";
                binding.notice.setText(notice);
            } else {
                binding.notice.setVisibility(View.GONE);
            }
            LogUtils.json("update", tripSoc);
            initGrid(tripSoc);
        });
        trackViewModel.startAddTrack();
        startMapTrack();
        chargeWarn();
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
        items.add(tripSoc.getEnergy() + ",kWh,消耗电量");
        items.add(AMapUtil.formatDouble1(tripSoc.getRmbPublich()) + ",RMB,充电成本（公共充电）");
        items.add(AMapUtil.formatDouble1(tripSoc.getRmbPrivate()) + ",RMB,充电成本（私人充电）");
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

    private void chargeWarn() {
        trackViewModel.getChargeWarn().observe(this, chargeWarn -> {
            final boolean[] isCharge = {false};
            NotificationUtils.notify(1, param -> {
                param.setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("充电提醒")
                        .setContentText("当前剩余电量低于" + chargeWarn.getTripSoc().getSoc() + "%，请前往站点进行充电")
                        .setAutoCancel(true);
                return null;
            });
            MyDialog myDialog = new MyDialog.Builder(activity)
                    .setContent("您好，当前剩余电量低于" + chargeWarn.getTripSoc().getSoc() + "%， 请前往站点进行充电")
                    .setCancelText("忽略")
                    .setSureText("我要充电")
                    .setSureListener(v -> {
                        EmMainActivity mainActivity = (EmMainActivity) activity;
                        mainActivity.selectTab(3);
                        EventBus.getDefault().post(new ToChargeEvent());
                        if (!isCharge[0]) {
                            charge(chargeWarn.getTripPoint());
                            isCharge[0] = true;
                        }
                    })
                    .create();
            myDialog.show();
            new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int second = (int) (millisUntilFinished / 1000);
                    String text = "我要充电(" + second + ")";
                    myDialog.getSureButton().setText(text);
                }

                public void onFinish() {
                    myDialog.dismiss();
                    if (!isCharge[0]) {
                        charge(chargeWarn.getTripPoint());
                        isCharge[0] = true;
                    }
                }
            }.start();
        });
    }

    private void charge(TripPoint tripPoint) {
        trackViewModel.charge(tripPoint).observe(this, new MyObserver<TripSoc>() {
            @Override
            public void onSuccess(TripSoc tripSoc) {
            }
        });
    }
}
