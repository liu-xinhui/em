package com.hold.electrify.bean;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * 行程
 */
@Data
public class Trip {

    //主键ID
    private String id;

    //用户ID
    private String userId;

    //车型ID
    private String carModelId;

    //温度(度)
    private int temperature = 0;

    //提醒充电阈值，百分比分子取整
    private int warningLevel = 30;

    //起点时间戳(ms)
    private long startTimestamp = 0;

    //起点经度(度)
    private double startLongitude = 0;

    //起点纬度(度)
    private double startLatitude = 0;

    //起点地址
    private String startAddress;

    //起点SOC,百分比分子取整
    private int startSoc = 100;

    //终点时间戳(ms)
    private long destTimestamp = 0;

    //终点经度(度)
    private double destLongitude = 0;

    //终点纬度(度)
    private double destLatitude = 0;

    //终点地址
    private String destAddress;

    //终点SOC,百分比分子取整
    private int destSoc = 0;

    //里程(km)
    private double mileage = 0;

    //时长(h)
    private double duration = 0;

    //平均速度(km/h)
    private double avSpeed = 0;

    //消耗电量(kwh)
    private double energy = 0;

    //充电成本(公),单位:元、RMB
    private double rmbPublich = 0;

    //充电成本(私),单位:元、RMB
    private double rmbPrivate = 0;

    //充电次数(次)
    private int chargeTimes = 0;

    //轨迹点列表
    private List<TripPoint> tripPoints = new ArrayList<>();

    //充电点列表
    private List<TripPoint> chargePoints = new ArrayList<>();
}
