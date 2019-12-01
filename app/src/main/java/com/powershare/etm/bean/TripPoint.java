package com.powershare.etm.bean;

import java.io.Serializable;

import lombok.Data;

/**
 * 行程中的轨迹点
 */
@Data
public class TripPoint implements Serializable {
    //主键ID
    private String id;

    //是否充电点
    private boolean chargePoint = false;

    //行程ID
    private String tripId;

    //时间戳(ms)
    private long timestamp = 0;

    //经度(度)
    private double longitude = 0;

    //纬度(度)
    private double latitude = 0;

    //速度(km/h)
    private double speed = 0;

    //已行驶距离(km)
    private double mileage = 0;

    //地址
    private String address;

    //方向角
    private double ag = 0;
}
