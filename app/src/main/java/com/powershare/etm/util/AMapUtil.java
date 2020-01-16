package com.powershare.etm.util;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.blankj.utilcode.util.LogUtils;
import com.powershare.etm.bean.TripPoint;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AMapUtil {
    /**
     * 把LatLonPoint对象转化为LatLon对象
     */
    public static LatLng convertToLatLng(LatLonPoint latLonPoint) {
        return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
    }

    public static List<LatLng> convertToLatLng(List<TripPoint> tripPoints) {
        List<LatLng> result = new ArrayList<>();
        if (tripPoints != null) {
            for (TripPoint tripPoint : tripPoints) {
                result.add(new LatLng(tripPoint.getLatitude(), tripPoint.getLongitude()));
            }
        }
        return result;
    }

    //转换为KM
    public static String mToKm(int lenMeter) {
        float dis = (float) lenMeter / 1000;
        return formatFloat(dis);
    }

    //转换为小时
    public static String secondToHour(int second) {
        float dis = (float) second / 3600;
        return formatFloat(dis);
    }

    //平均速度
    public static String speed(String distanceStr, String hourStr) {
        float distance = Float.parseFloat(distanceStr);
        float hour = Float.parseFloat(hourStr);
        float dis = distance / hour;
        return formatFloat(dis);
    }

    public static String formatFloat(float src) {
        DecimalFormat numF = new DecimalFormat("#.##");
        return numF.format(src);
    }

    public static String formatDouble(double src) {
        DecimalFormat numF = new DecimalFormat("#.##");
        LogUtils.d(src,numF.format(src));
        return numF.format(src);
    }
}
