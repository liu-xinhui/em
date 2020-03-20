package com.powershare.etm.ui.tab2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProviders;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.UiSettings;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.powershare.etm.R;
import com.powershare.etm.bean.Trip;
import com.powershare.etm.bean.TripPoint;
import com.powershare.etm.databinding.ActivityTrackDetailBinding;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.ui.route.TrackDetailOverlay;
import com.powershare.etm.util.AMapUtil;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.vm.TrackViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TrackDetailActivity extends BaseActivity {
    private ActivityTrackDetailBinding binding;
    private TrackViewModel trackViewModel;
    private AMap aMap;

    public static void go(Activity activity, String trickId) {
        Intent intent = new Intent(activity, TrackDetailActivity.class);
        intent.putExtra("trickId", trickId);
        activity.startActivity(intent);
    }

    @Override
    protected View initContentView() {
        binding = ActivityTrackDetailBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.map.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.map.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.map.onSaveInstanceState(outState);
    }

    private void initTopBar() {
        binding.topBar.setTitle("行程追踪详情");
        binding.topBar.setBackgroundAlpha(1);
        binding.topBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

    @Override
    protected void createViewModel() {
        trackViewModel = ViewModelProviders.of(this).get(TrackViewModel.class);
    }

    @Override
    protected void onMounted(Bundle savedInstanceState) {
        initTopBar();
        //取值
        Intent intent = getIntent();
        String trickId = intent.getStringExtra("trickId");
        if (TextUtils.isEmpty(trickId)) {
            CommonUtil.showToast("未知错误");
            return;
        }
        new Handler().postDelayed(() -> {
            binding.map.onCreate(savedInstanceState);
            aMap = binding.map.getMap();
            UiSettings uiSettings = aMap.getUiSettings();
            uiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
            uiSettings.setZoomControlsEnabled(false);
            uiSettings.setRotateGesturesEnabled(false);
            binding.mapContainer.setScrollView(binding.scrollView);
            traceGet(trickId);
        }, 100);
        binding.fullScreen.setOnClickListener(v -> {
            int height = SizeUtils.dp2px(260);
            ViewGroup.LayoutParams layoutParams = binding.mapContainer.getLayoutParams();
            if (layoutParams.height == height) {
                layoutParams.height = ScreenUtils.getAppScreenHeight() - SizeUtils.dp2px(94);
            } else {
                layoutParams.height = height;
            }
            binding.mapContainer.setLayoutParams(layoutParams);
        });
    }

    //请求后台
    private void traceGet(String trackId) {
        trackViewModel.traceGet(trackId).observe(this, new MyObserver<Trip>() {
            @Override
            public void onStart() {
                showLoading();
            }

            @Override
            public void onSuccess(Trip trip) {
                hideLoading();
                String powerValueStart = trip.getStartSoc() + "%";
                binding.powerValueStart.setText(powerValueStart);
                //
                String powerValueEnd = trip.getDestSoc() + "%";
                binding.powerValueEnd.setText(powerValueEnd);
                if (trip.getDestSoc() >= 30) {
                    binding.powerEnd.setImageResource(R.mipmap.power_white);
                } else {
                    binding.powerEnd.setImageResource(R.mipmap.power_end);
                }
                //
                String powerConsumption = "行程消耗" + AMapUtil.formatDouble(trip.getEnergy()) + "kWh电量";
                binding.powerConsumption.setText(powerConsumption);
                //提示
                if (CollectionUtils.isEmpty(trip.getChargePoints())) {
                    binding.notice.setText("恭喜您！途中无需充电~");
                } else {
                    String text = "途中需要充电" + trip.getChargePoints().size() + "次~";
                    binding.notice.setText(text);
                }

                List<String> items = new ArrayList<>();
                String distance = AMapUtil.formatDouble(trip.getMileage());
                String hour = AMapUtil.formatDouble(trip.getDuration());
                items.add(distance + ",km,总里程");
                items.add(AMapUtil.formatDouble(trip.getEnergy()) + ",kWh,消耗电量");
                items.add(hour + ",h,行驶时间");
                items.add(AMapUtil.formatDouble(trip.getAvSpeed()) + ",km/h,平均速度");
                items.add(AMapUtil.formatDouble1(trip.getRmbPublich()) + ",RMB,充电成本（公共充电）");
                items.add(AMapUtil.formatDouble1(trip.getRmbPrivate()) + ",RMB,充电成本（私人充电）");
                binding.infoContainer.removeAllViews();
                for (String item : items) {
                    String[] itemArr = item.split(",");
                    View view = LayoutInflater.from(TrackDetailActivity.this).inflate(R.layout.item_title_value, null);
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
                initMapTrack(trip);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                hideLoading();
            }
        });
    }

    private void initMapTrack(Trip trip) {
        //地图
        aMap.clear();
        List<TripPoint> tripPoints = trip.getTripPoints();
        if (CollectionUtils.isEmpty(tripPoints)) {
            return;
        }
        TrackDetailOverlay trackDetailOverlay = new TrackDetailOverlay(this, aMap, tripPoints, trip.getChargePoints());
        trackDetailOverlay.removeFromMap();
        trackDetailOverlay.addToMap();
        trackDetailOverlay.zoomToSpan();
    }
}
