package com.powershare.etm.ui.tab3;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProviders;

import com.amap.api.maps.AMap;
import com.amap.api.maps.UiSettings;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.powershare.etm.R;
import com.powershare.etm.bean.Location;
import com.powershare.etm.bean.PredictCharge;
import com.powershare.etm.bean.TripParam;
import com.powershare.etm.bean.TripPoint;
import com.powershare.etm.databinding.ActivityPredictBinding;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.ui.tab3.route.DrivingRouteOverlay;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.vm.PredictViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.amap.api.services.route.RouteSearch.DRIVING_SINGLE_DEFAULT;

public class PredictActivity extends BaseActivity {
    private ActivityPredictBinding binding;
    private PredictViewModel predictViewModel;
    private AMap aMap;

    @Override
    protected View initContentView() {
        binding = ActivityPredictBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding.map.onCreate(savedInstanceState);
        aMap = binding.map.getMap();
        UiSettings uiSettings = aMap.getUiSettings();
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
        binding.topBar.setTitle("行程预估结果");
        binding.topBar.setBackgroundAlpha(1);
        binding.topBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

    @Override
    protected void createViewModel() {
        predictViewModel = ViewModelProviders.of(this).get(PredictViewModel.class);
    }

    @Override
    protected void onMounted() {
        initTopBar();
        //取值
        //TripParam tripParam = getIntent().getParcelableExtra("tripParam");
        TripParam tripParam = new TripParam();

        //121.000919,31.480908
        TripPoint start = new TripPoint();
        start.setLongitude(121.000919);
        start.setLatitude(31.480908);
        tripParam.setStartPoint(start);
        //120.768832,30.871108
        TripPoint end = new TripPoint();
        end.setLongitude(121.010919);
        end.setLatitude(31.580908);
        tripParam.setDestPoint(end);

        tripParam.setWarningLevel(10);
        tripParam.setTemperature(20);
        tripParam.setCarModelId("426eb1d5-0c63-11ea-b5a8-fa163e419dee");
        this.tracePredict(tripParam);
    }

    //请求后台
    private void tracePredict(TripParam tripParam) {
        predictViewModel.tracePredict(tripParam).observe(this, new MyObserver<PredictCharge>() {
            @Override
            public void onSuccess(PredictCharge predictCharge) {
                mapRoute(tripParam, predictCharge);
            }
        });
    }

    //显示路线
    private void mapRoute(TripParam tripParam, PredictCharge predictCharge) {
        RouteSearch routeSearch = new RouteSearch(this);
        routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
            @Override
            public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

            }

            @Override
            public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int code) {
                if (code == 1000) {
                    initUi(predictCharge, driveRouteResult);
                } else {
                    CommonUtil.showErrorToast("出错");
                }
            }

            @Override
            public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

            }

            @Override
            public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

            }
        });
        //起点终点
        LatLonPoint startPoint = new LatLonPoint(tripParam.getStartPoint().getLatitude(), tripParam.getStartPoint().getLongitude());
        LatLonPoint endPoint = new LatLonPoint(tripParam.getDestPoint().getLatitude(), tripParam.getDestPoint().getLongitude());
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
        //途经点
        List<Location> chargeLocationList = predictCharge.getChargeLocationList();
        List<LatLonPoint> passedByPoints = null;
        if (CollectionUtils.isNotEmpty(chargeLocationList)) {
            for (Location location : chargeLocationList) {
                passedByPoints = new ArrayList<>();
                passedByPoints.add(new LatLonPoint(location.getLatitude(), location.getLongitude()));
            }
        }
        //fromAndTo包含路径规划的起点和终点，drivingMode表示驾车模式
        //第三个参数表示途经点（最多支持16个），第四个参数表示避让区域（最多支持32个），第五个参数表示避让道路
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, DRIVING_SINGLE_DEFAULT, passedByPoints, null, "");
        //发送请求
        routeSearch.calculateDriveRouteAsyn(query);
    }

    private void initUi(PredictCharge predictCharge, DriveRouteResult driveRouteResult) {
        //地图
        if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
            if (driveRouteResult.getPaths().size() > 0) {
                final DrivePath drivePath = driveRouteResult.getPaths().get(0);
                DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(this, aMap, drivePath, driveRouteResult.getStartPos(), driveRouteResult.getTargetPos(), null);
                drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
                drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                drivingRouteOverlay.removeFromMap();
                drivingRouteOverlay.addToMap();
                drivingRouteOverlay.zoomToSpan();
                int dis = (int) drivePath.getDistance();
                int dur = (int) drivePath.getDuration();
                List<String> items = new ArrayList<>();
                String distance = CommonUtil.mToKm(dis);
                String hour = CommonUtil.secondToHour(dur);
                items.add(distance + ",km,总里程");
                items.add(predictCharge.getEnergy() + ",%,消耗电量");
                items.add(hour + ",H,行驶时间");
                items.add(CommonUtil.speed(distance, hour) + ",KM/H,平均速度");
                items.add(predictCharge.getRmbPublich() + ",RMB,充电成本（公共充电）");
                items.add(predictCharge.getRmbPrivate() + ",RMB,充电成本（私人充电）");
                for (String item : items) {
                    String[] itemArr = item.split(",");
                    View view = LayoutInflater.from(PredictActivity.this).inflate(R.layout.item_title_value, null);
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
