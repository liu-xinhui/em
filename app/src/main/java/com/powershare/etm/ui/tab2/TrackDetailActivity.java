package com.powershare.etm.ui.tab2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProviders;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.UiSettings;
import com.amap.api.services.route.DrivePath;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.powershare.etm.R;
import com.powershare.etm.bean.Trip;
import com.powershare.etm.bean.TripPoint;
import com.powershare.etm.databinding.ActivityTrackDetailBinding;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.ui.route.DrivingRouteOverlay;
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

    @Override
    protected View initContentView() {
        binding = ActivityTrackDetailBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.map.onCreate(savedInstanceState);
        aMap = binding.map.getMap();
        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        binding.mapContainer.setScrollView(binding.scrollView);
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
        binding.topBar.setTitle("行程预测详情");
        binding.topBar.setBackgroundAlpha(1);
        binding.topBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

    @Override
    protected void createViewModel() {
        trackViewModel = ViewModelProviders.of(this).get(TrackViewModel.class);
    }

    @Override
    protected void onMounted() {
        initTopBar();
        //取值
        Intent intent = getIntent();
        String trickId = intent.getStringExtra("trickId");
        if (trickId == null) {
            CommonUtil.showErrorToast("未知错误");
            return;
        }
        traceGet(trickId);
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
                //
                String powerConsumption = "行程消耗" + (trip.getStartSoc() - trip.getDestSoc()) + "%电量";
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
                items.add(trip.getEnergy() + ",kwh,消耗电量");
                items.add(hour + ",H,行驶时间");
                items.add(AMapUtil.formatDouble(trip.getAvSpeed()) + ",KM/H,平均速度");
                items.add(trip.getRmbPublich() + ",RMB,充电成本（公共充电）");
                items.add(trip.getRmbPrivate() + ",RMB,充电成本（私人充电）");
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
        LogUtils.json(tripPoints);
        TrackDetailOverlay trackDetailOverlay = new TrackDetailOverlay(this, aMap, tripPoints, trip.getChargePoints());
        trackDetailOverlay.removeFromMap();
        trackDetailOverlay.addToMap();
        trackDetailOverlay.zoomToSpan();
    }
}
