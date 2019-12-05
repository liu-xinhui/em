package com.powershare.etm.ui.route;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.powershare.etm.R;
import com.powershare.etm.bean.TripPoint;
import com.powershare.etm.util.AMapUtil;

import java.util.List;

/**
 * 导航路线图层类。
 */
public class TrackDetailOverlay extends RouteOverlay {

    private List<LatLng> tripPointList;
    private List<LatLng> throughPointList;
    private PolylineOptions mPolylineOptions;

    /**
     * 根据给定的参数，构造一个导航路线图层类对象。
     */
    public TrackDetailOverlay(Context context, AMap amap, List<TripPoint> tripPoints, List<TripPoint> chargePoints) {
        super(context);
        mAMap = amap;
        tripPointList = AMapUtil.convertToLatLng(tripPoints);
        throughPointList = AMapUtil.convertToLatLng(chargePoints);
        if (tripPointList.size() > 2) {
            startPoint = tripPointList.get(0);
            endPoint = tripPointList.get(tripPoints.size() - 1);
        }
    }

    /**
     * 添加轨迹添加到地图上显示。
     */
    public void addToMap() {
        initPolylineOptions();
        try {
            if (mAMap == null || tripPointList.size() < 2) {
                return;
            }
            mPolylineOptions.add(startPoint);
            mPolylineOptions.addAll(tripPointList);
            mPolylineOptions.add(endPoint);
            if (startMarker != null) {
                startMarker.remove();
                startMarker = null;
            }
            if (endMarker != null) {
                endMarker.remove();
                endMarker = null;
            }
            addStartAndEndMarker();
            addThroughPointMarker();
            showPolyline();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化线段属性
     */
    private void initPolylineOptions() {
        mPolylineOptions = null;
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(getDriveColor()).width(getRouteWidth());
    }

    private void showPolyline() {
        addPolyLine(mPolylineOptions);
    }

    public LatLng convertToLatLng(LatLonPoint point) {
        return new LatLng(point.getLatitude(), point.getLongitude());
    }

    @Override
    protected LatLngBounds getLatLngBounds() {
        LatLngBounds.Builder b = LatLngBounds.builder();
        b.include(new LatLng(startPoint.latitude, startPoint.longitude));
        b.include(new LatLng(endPoint.latitude, endPoint.longitude));
        if (this.throughPointList != null && this.throughPointList.size() > 0) {
            for (int i = 0; i < this.throughPointList.size(); i++) {
                b.include(this.throughPointList.get(i));
            }
        }
        return b.build();
    }

    private void addThroughPointMarker() {
        if (this.throughPointList != null && this.throughPointList.size() > 0) {
            for (int i = 0; i < this.throughPointList.size(); i++) {
                mAMap.addMarker((new MarkerOptions())
                        .position(throughPointList.get(i))
                        .visible(true)
                        .icon(getThroughPointBitDes())
                        .title("充电站"));
            }
        }
    }

    private BitmapDescriptor getThroughPointBitDes() {
        return BitmapDescriptorFactory.fromResource(R.mipmap.charging_station);
    }
}