package com.powershare.etm.ui.tab3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.UiSettings;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.bumptech.glide.Glide;
import com.powershare.etm.R;
import com.powershare.etm.bean.CarModel;
import com.powershare.etm.bean.Charge;
import com.powershare.etm.bean.PredictCharge;
import com.powershare.etm.bean.TripParam;
import com.powershare.etm.databinding.ActivityPredictFullBinding;
import com.powershare.etm.ui.base.BaseActivity;
import com.powershare.etm.ui.route.DrivingRouteOverlay;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.GlobalValue;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.vm.CarViewModel;
import com.powershare.etm.vm.PredictViewModel;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amap.api.services.route.RouteSearch.DRIVING_SINGLE_DEFAULT;

public class PredictFullActivity extends BaseActivity {
    private ActivityPredictFullBinding binding;
    private PredictViewModel predictViewModel;
    private CarViewModel carViewModel;
    private AMap aMap;
    private TripParam tripParam;

    @Override
    protected View initContentView() {
        binding = ActivityPredictFullBinding.inflate(getLayoutInflater());
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
        binding.topBar.setTitle("行程预估结果");
        binding.topBar.setBackgroundAlpha(1);
        binding.topBar.addLeftBackImageButton().setOnClickListener(v -> finish());
    }

    @Override
    protected void createViewModel() {
        predictViewModel = ViewModelProviders.of(this).get(PredictViewModel.class);
        carViewModel = ViewModelProviders.of(this).get(CarViewModel.class);
    }

    @Override
    protected void onMounted(Bundle savedInstanceState) {
        initTopBar();
        initMap(savedInstanceState);
        setChargeVisibility(false);
        //取值
        Intent intent = getIntent();
        tripParam = (TripParam) intent.getSerializableExtra("tripParam");
        PredictCharge predictCharge = (PredictCharge) intent.getSerializableExtra("predictCharge");
        //DriveRouteResult driveRouteResult = intent.getParcelableExtra("driveRouteResult");

        //
        Charge charge = (Charge) intent.getSerializableExtra("charge");
        if (charge != null) {
            showCharge(charge);
        }

        DriveRouteResult driveRouteResult = GlobalValue.getDriveRouteResult();
        new Handler().postDelayed(() -> initUi(predictCharge, driveRouteResult), 500);
        //this.tracePredict(tripParam);
        //车型点击
        getCarListData();
        //温度点击
        getTemp();
        //全屏点击
        binding.fullScreen.setOnClickListener(view -> {
            setChargeVisibility(false);
        });
        //导航点击
        binding.nav.setOnClickListener(view -> {
            String gaode = "高德地图";
            String baidu = "百度地图";
            double destLat = tripParam.getDestPoint().getLatitude();
            double destLng = tripParam.getDestPoint().getLongitude();
            String destName = "充电桩";
            new QMUIBottomSheet.BottomListSheetBuilder(this)
                    .addItem(gaode)
                    .addItem(baidu)
                    .setOnSheetItemClickListener((dialog, itemView, position, tag) -> {
                        dialog.dismiss();
                        if (gaode.equals(tag)) {
                            if (AppUtils.isAppInstalled("com.autonavi.minimap")) {
                                Uri uri = Uri.parse("amapuri://route/plan/?dlat=" + destLat + "&dlon=" + destLng + "&dname=" + destName + "&dev=0&t=0");
                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            } else {
                                CommonUtil.showToast("未安装" + gaode);
                            }
                        } else {
                            if (AppUtils.isAppInstalled("com.baidu.BaiduMap")) {
                                Uri uri = Uri.parse("baidumap://map/direction?destination=latlng:" + destLat + "," + destLng + "|name:" + destName + "&mode=driving&coord_type=gcj02");
                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            } else {
                                CommonUtil.showToast("未安装" + baidu);
                            }
                        }
                    }).build().show();
        });
    }

    private void initMap(Bundle savedInstanceState) {
        binding.map.onCreate(savedInstanceState);
        aMap = binding.map.getMap();
        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        aMap.setOnMarkerClickListener(marker -> {
            Object obj = marker.getObject();
            if (obj instanceof Charge) {
                Charge charge = (Charge) marker.getObject();
                showCharge(charge);
            }
            return true;
        });
    }

    private void showCharge(Charge charge) {
        binding.name.setText(charge.getName());
        binding.address.setText(charge.getAddress());
        binding.price.setText(charge.getPrice());
        binding.fastCount.setText(charge.getDcTotalAvailableNum() + "/" + charge.getDcTotalNum());
        binding.slowCount.setText(charge.getAcTotalAvailableNum() + "/" + charge.getAcTotalNum());
        Glide.with(PredictFullActivity.this)
                .load(charge.getLogoUrl())
                .error(R.mipmap.charging_station)
                .placeholder(R.mipmap.charging_station)
                .into(binding.icon);
        setChargeVisibility(true);
    }

    //请求后台
    private void tracePredict(TripParam tripParam) {
        predictViewModel.tracePredict(tripParam).observe(this, new MyObserver<PredictCharge>() {
            @Override
            public void onStart() {
                showLoading();
            }

            @Override
            public void onSuccess(PredictCharge predictCharge) {
                mapRoute(tripParam, predictCharge);
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                hideLoading();
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
                hideLoading();
                if (code == 1000) {
                    initUi(predictCharge, driveRouteResult);
                } else {
                    CommonUtil.showToast("出错");
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
        List<Charge> chargeLocationList = predictCharge.getChargeLocationList();
        List<LatLonPoint> passedByPoints = null;
        if (CollectionUtils.isNotEmpty(chargeLocationList)) {
            passedByPoints = new ArrayList<>();
            for (Charge location : chargeLocationList) {
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
        aMap.clear();
        if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
            if (driveRouteResult.getPaths().size() > 0) {
                final DrivePath drivePath = driveRouteResult.getPaths().get(0);
                DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(this, aMap, drivePath, driveRouteResult.getStartPos(), driveRouteResult.getTargetPos(), predictCharge.getChargeLocationList());
                drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
                drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                drivingRouteOverlay.removeFromMap();
                drivingRouteOverlay.addToMap();
                drivingRouteOverlay.zoomToSpan();
            } else if (driveRouteResult.getPaths() == null) {
                CommonUtil.showToast("无结果");
            }
        } else {
            CommonUtil.showToast("无结果");
        }
    }

    private void getCarListData() {
        List<CarModel> mCarModels = new ArrayList<>();
        QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(this)
                .setOnSheetItemClickListener((dialog, itemView, position, tag) -> {
                    dialog.dismiss();
                    tripParam.setCarModelId(mCarModels.get(position).getId());
                    this.tracePredict(tripParam);
                });
        binding.car.setOnClickListener(view -> {
            if (mCarModels.size() == 0) {
                //车辆列表数据
                carViewModel.carList(false).observe(PredictFullActivity.this, new MyObserver<List<CarModel>>() {
                    @Override
                    public void onStart() {
                        showLoading();
                    }

                    @Override
                    public void onSuccess(List<CarModel> carModels) {
                        mCarModels.clear();
                        mCarModels.addAll(carModels);
                        for (CarModel carModel : carModels) {
                            builder.addItem(carModel.getName());
                        }
                        builder.build().show();
                    }

                    @Override
                    public void onFinish() {
                        hideLoading();
                    }
                });
            } else {
                builder.build().show();
            }
        });
    }

    private void getTemp() {
        List<String> items = new ArrayList<>();
        Map<String, Integer> tempMap = new HashMap<>();
        int index = 0;
        for (int i = -20; i <= 50; i++) {
            items.add(i + "℃");
            tempMap.put(i + "", index);
            index++;
        }
        OptionsPickerView<String> tempOptions = new OptionsPickerBuilder(this, (options1, option2, options3, v) -> {
            int temp = Integer.parseInt(items.get(options1).replace("℃", ""));
            tripParam.setTemperature(temp);
            tracePredict(tripParam);
        }).build();
        tempOptions.setPicker(items);

        View.OnClickListener tempSelect = view -> {
            String currTempStr = tripParam.getTemperature() + "";
            Integer value = tempMap.get(currTempStr);
            if (value != null) {
                tempOptions.setSelectOptions(value);
            }
            tempOptions.show();
        };

        binding.temp.setOnClickListener(tempSelect);
    }

    private void setChargeVisibility(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        binding.chargeInfo.setVisibility(visibility);
        binding.nav.setVisibility(visibility);
        binding.fullScreen.setVisibility(visibility);
    }
}
