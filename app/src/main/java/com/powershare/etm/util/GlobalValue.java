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
    private static int trackMileage;

    public static void setTracking(boolean tracking) {
        GlobalValue.tracking = tracking;
        GlobalValue.trackMileage = 0;
    }
}
