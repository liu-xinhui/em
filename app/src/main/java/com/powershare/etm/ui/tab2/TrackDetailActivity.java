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
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Tip;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.powershare.etm.R;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.bean.Location;
import com.powershare.etm.bean.PredictCharge;
import com.powershare.etm.bean.Trip;
import com.powershare.etm.bean.TripParam;
import com.powershare.etm.databinding.ActivityPredictBinding;
import com.powershare.etm.databinding.ActivityTrackDetailBinding;
import com.powershare.etm.databinding.ActivityTrackListBinding;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.ui.route.DrivingRouteOverlay;
import com.powershare.etm.ui.tab3.PredictFullActivity;
import com.powershare.etm.util.AMapUtil;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.vm.CarViewModel;
import com.powershare.etm.vm.PredictViewModel;
import com.powershare.etm.vm.TrackViewModel;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.amap.api.services.route.RouteSearch.DRIVING_SINGLE_DEFAULT;

public class TrackDetailActivity extends BaseActivity {
    private ActivityTrackDetailBinding binding;
    private TrackViewModel trackViewModel;
    private AMap aMap;
    private String trickId;

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
        trickId = intent.getStringExtra("trickId");
        if (trickId == null) {
            CommonUtil.showErrorToast("未知错误");
            return;
        }
    }

    //请求后台
    private void tracePredict(TripParam tripParam) {
/*        trackViewModel.tracePredict(tripParam).observe(this, new MyObserver<PredictCharge>() {
            @Override
            public void onStart() {
                showLoading();
            }

            @Override
            public void onSuccess(PredictCharge predictCharge) {
                String powerValueStart = predictCharge.getStartSoc() + "%";
                binding.powerValueStart.setText(powerValueStart);
                //
                String powerValueEnd = predictCharge.getDestSoc() + "%";
                binding.powerValueEnd.setText(powerValueEnd);
                //
                String powerConsumption = "行程消耗" + (predictCharge.getStartSoc() - predictCharge.getDestSoc()) + "%电量";
                binding.powerConsumption.setText(powerConsumption);
                //提示
                if (CollectionUtils.isEmpty(predictCharge.getChargeLocationList())) {
                    binding.notice.setText("恭喜您！途中无需充电~");
                } else {
                    String text = "途中需要充电" + predictCharge.getChargeLocationList().size() + "次~";
                    binding.notice.setText(text);
                }
                mapRoute(tripParam, predictCharge);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                hideLoading();
            }
        });*/
    }

    private void initUi(PredictCharge predictCharge, DriveRouteResult driveRouteResult) {
        //地图
        aMap.clear();
        if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
            if (driveRouteResult.getPaths().size() > 0) {
                final DrivePath drivePath = driveRouteResult.getPaths().get(0);
                DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(this, aMap, drivePath, driveRouteResult.getStartPos(), driveRouteResult.getTargetPos(), driveRouteResult.getDriveQuery().getPassedByPoints());
                drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
                drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                drivingRouteOverlay.removeFromMap();
                drivingRouteOverlay.addToMap();
                drivingRouteOverlay.zoomToSpan();
                int dis = (int) drivePath.getDistance();
                int dur = (int) drivePath.getDuration();
                List<String> items = new ArrayList<>();
                String distance = AMapUtil.mToKm(dis);
                String hour = AMapUtil.secondToHour(dur);
                items.add(distance + ",km,总里程");
                items.add(predictCharge.getEnergy() + ",kwh,消耗电量");
                items.add(hour + ",H,行驶时间");
                items.add(AMapUtil.speed(distance, hour) + ",KM/H,平均速度");
                items.add(predictCharge.getRmbPublich() + ",RMB,充电成本（公共充电）");
                items.add(predictCharge.getRmbPrivate() + ",RMB,充电成本（私人充电）");
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
            } else if (driveRouteResult.getPaths() == null) {
                CommonUtil.showErrorToast("无结果");
            }
        } else {
            CommonUtil.showErrorToast("无结果");
        }
    }
}
