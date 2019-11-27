package com.hold.electrify.bean;

import java.io.Serializable;

import lombok.Data;

/**
 * 车型
 */
@Data
public class CarModel implements Serializable {

    //主键ID
    private String id;

    //车型编码
    private String carModelCode;

    //排序序号
    private int orderSeq = 0;

    //名称
    private String name;

    //续航里程(km)
    private double maxSoeKm = 0;

    //电池容量(kwh)
    private double maxSoeKwh = 0;

    //能量消耗(L/100km)
    private double cafc = 0;

    //每100km充电时长(min(AC))
    private double chargeTimeKm100 = 0;

    //最高速度(km/h)
    private double maxSpeed = 0;

    //加速度(0-100km/h),单位: s
    private double accTime = 0;

    //图片ID集合
    private String[] photoIds = new String[0];
}

