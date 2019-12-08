package com.powershare.etm.util;


import lombok.Getter;
import lombok.Setter;


public class GlobalValue {
    //是否追踪中
    @Getter
    private static boolean tracking;
    //已经追踪的里程（m)
    @Getter
    @Setter
    private static int trackMileage = 50000;
    //是否已经开始跳转到登录页面，防止重复跳转登录页
    private static long lastToLoginTime;

    public static void setTracking(boolean tracking) {
        GlobalValue.tracking = tracking;
        GlobalValue.trackMileage = 50000;
    }

    public static String getTrackMileageKm() {
        float result = trackMileage / 1000f;
        return AMapUtil.formatFloat(result);
    }

    public synchronized static boolean isNeedGotoLogin() {
        long curr = System.currentTimeMillis();
        long diff = curr - lastToLoginTime;
        lastToLoginTime = curr;
        return diff < 2000;
    }
}
