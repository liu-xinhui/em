package com.powershare.etm.ui.tabmap;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.VisibleRegion;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Tip;
import com.blankj.utilcode.util.AppUtils;
import com.bumptech.glide.Glide;
import com.powershare.etm.R;
import com.powershare.etm.bean.ApiResult;
import com.powershare.etm.bean.Charge;
import com.powershare.etm.bean.SimpleLocation;
import com.powershare.etm.databinding.FragmentTabMapBinding;
import com.powershare.etm.event.ToChargeEvent;
import com.powershare.etm.ui.base.BaseFragment;
import com.powershare.etm.ui.tab3.SearchLocActivity;
import com.powershare.etm.util.AMapUtil;
import com.powershare.etm.util.CommonUtil;
import com.powershare.etm.util.MyObserver;
import com.powershare.etm.util.SearchLocHistoryHelper;
import com.powershare.etm.vm.AMapViewModel;
import com.powershare.etm.vm.TrackViewModel;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.powershare.etm.bean.SimpleLocation.TYPE_NORMAL;

public class TabMapFragment extends BaseFragment {

    private FragmentTabMapBinding binding;
    private TrackViewModel trackViewModel;
    private AMapViewModel aMapViewModel;
    private AMap aMap;
    private LiveData<ApiResult<List<SimpleLocation>>> chargeLiveData;
    private MarkerOptions currentAddressMarker;

    public static TabMapFragment newInstance() {
        return new TabMapFragment();
    }

    @Override
    protected View initContentView(LayoutInflater inflater) {
        binding = FragmentTabMapBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void createViewModel() {
        trackViewModel = ViewModelProviders.of(activity).get(TrackViewModel.class);
        aMapViewModel = ViewModelProviders.of(activity).get(AMapViewModel.class);
    }

    @Override
    protected void onMounted() {
        View.OnClickListener onClickListener = view -> {
            Intent intent = new Intent(activity, SearchLocActivity.class);
            intent.putExtra("type", 1);
            intent.putExtra("module", 2);
            startActivityForResult(intent, 1);
        };
        binding.searchBar.setOnClickListener(onClickListener);
        //导航点击
        binding.nav.setOnClickListener(view -> {
            String gaode = "高德地图";
            String baidu = "百度地图";
            Charge charge = (Charge) view.getTag();
            double destLat = charge.getLatitude();
            double destLng = charge.getLongitude();
            String destName = "充电桩";
            new QMUIBottomSheet.BottomListSheetBuilder(activity)
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
        binding.currentPosition.setOnClickListener(view -> aMapViewModel.currentLoc().observe(this, aMapLocation -> {
            LatLng latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
        }));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.map.onCreate(savedInstanceState);
        aMap = binding.map.getMap();
        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
        uiSettings.setZoomControlsEnabled(false);
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                binding.chargeInfo.setVisibility(View.GONE);
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                VisibleRegion visibleRegion = aMap.getProjection().getVisibleRegion();
                LatLng farLeft = visibleRegion.farLeft;
                LatLng nearRight = visibleRegion.nearRight;
                float radius = AMapUtils.calculateLineDistance(farLeft, nearRight) / 2000;
                getChargeList(cameraPosition.target, cameraPosition.zoom, radius);
            }
        });

        // 定义 Marker 点击事件监听
        // marker 对象被点击时回调的接口
        // 返回 true 则表示接口已响应事件，否则返回false
        AMap.OnMarkerClickListener markerClickListener = marker -> {
            String chargeId = (String) marker.getObject();
            if (binding.chargeInfo.getVisibility() != View.GONE) {
                binding.chargeInfo.setVisibility(View.GONE);
                return true;
            }
            if (!TextUtils.isEmpty(chargeId)) {
                getCharge(chargeId);
            }
            return true;
        };
        aMap.setOnMarkerClickListener(markerClickListener);
    }

    @Override
    protected void loadData() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//定位一次，且将视角移动到地图中心点。
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.position)));
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

        CameraUpdate mCameraUpdate = CameraUpdateFactory.zoomTo(8);
        aMap.animateCamera(mCameraUpdate);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 1 && data != null) {
            Tip item = data.getParcelableExtra("result");
            if (item != null) {
                SearchLocHistoryHelper.getInstance(2).addOneHistory(item);
                binding.searchBar.setText(item.getName());
                binding.searchBar.setTag(item);
                LatLonPoint point = item.getPoint();
                //参数依次是：视角调整区域的中心点坐标、希望调整到的缩放级别、俯仰角0°~45°（垂直与地图时为0）、偏航角 0~360° (正北方为0)
                LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
                CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, 14, 0, 0));
                aMap.animateCamera(mCameraUpdate);

                currentAddressMarker = new MarkerOptions();
                currentAddressMarker.position(latLng);
                currentAddressMarker.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.address)));
                aMap.addMarker(currentAddressMarker);
            }
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        binding.map.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        binding.map.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.map.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.map.onDestroy();
    }

    private void getChargeList(LatLng centerPoint, float mapLevel, float radius) {
        if (chargeLiveData != null) {
            chargeLiveData.removeObservers(this);
        }
        binding.tip.setVisibility(View.VISIBLE);
        chargeLiveData = trackViewModel.traceRadius(centerPoint.latitude, centerPoint.longitude, mapLevel, radius);
        chargeLiveData.observe(this, new MyObserver<List<SimpleLocation>>() {
            @Override
            public void onSuccess(List<SimpleLocation> result) {
                aMap.clear(true);
                if (currentAddressMarker != null) {
                    aMap.addMarker(currentAddressMarker);
                }
                for (SimpleLocation item : result) {
                    LatLng latLng = new LatLng(item.getLatitude(), item.getLongitude());
                    MarkerOptions options = new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), item.getType() == TYPE_NORMAL ? R.mipmap.charge_no_use : R.mipmap.charge_use)));
                    final Marker marker = aMap.addMarker(options);
                    marker.setObject(item.getId());

                    Animation animation = new ScaleAnimation(0, 1, 0, 1);
                    animation.setDuration(200);
                    animation.setInterpolator(new LinearInterpolator());

                    marker.setAnimation(animation);
                    marker.startAnimation();
                }
            }

            @Override
            public void onFinish() {
                binding.tip.setVisibility(View.GONE);
            }
        });
    }

    private void getCharge(String chargeId) {
        binding.navTip.setVisibility(View.VISIBLE);
        binding.chargeInfo.setVisibility(View.VISIBLE);
        trackViewModel.getCharge(chargeId).observe(this, new MyObserver<Charge>() {
            @Override
            public void onSuccess(Charge charge) {
                binding.nav.setTag(charge);
                binding.name.setText(charge.getName());
                binding.address.setText(charge.getAddress());
                binding.price.setText(AMapUtil.formatDouble2(charge.getPrice()));
                String fastCount = "" + charge.getDcTotalNum();
                binding.fastCount.setText(fastCount);
                String slowCount = "" + charge.getAcTotalNum();
                binding.slowCount.setText(slowCount);
                Glide.with(activity)
                        .load(charge.getLogoUrl())
                        .error(R.mipmap.charging_station)
                        .placeholder(R.mipmap.charging_station)
                        .into(binding.icon);
            }

            @Override
            public void onFinish() {
                binding.navTip.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onToChargeEvent(ToChargeEvent event) {
        aMapViewModel.currentLoc().observe(this, aMapLocation -> {
            LatLng latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        });
    }
}
