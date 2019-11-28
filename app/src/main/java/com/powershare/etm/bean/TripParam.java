package com.powershare.etm.bean;

import lombok.Data;

/**
 * 行程追踪参数
 */
@Data
public class TripParam {
    //车型ID
    private String carModelId;

    //充电提醒SOC,百分比分子取整
    private int warningLevel = 30;

    //温度(度)
    private int temperature = 20;

    //起点
    private TripPoint startPoint;

    //终点
    private TripPoint destPoint;
}
