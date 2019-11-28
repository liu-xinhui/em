package com.powershare.etm.bean;

import lombok.Data;

/**
 * 累计行程
 */
@Data
public class TotalTrip {
    //主键ID
    private String id;

    //用户ID
    private String userId;

    //累计次数,单位:次
    private int totalTimes = 0;

    //累计里程,单位:km
    private double totalMileage = 0;

    //累计时间,单位:h
    private long totalDuration = 0;

    //平均速度,单位:km/h
    private double avSpeed = 0;

    //累计消耗电量,单位:kwh
    private double totalEnergy = 0;

    //累计充电成本(公),单位:元、RMB
    private double totalRmbPublich = 0;

    //累计充电成本(私),单位:元、RMB
    private double totalRmbPrivate = 0;
}
