package com.powershare.etm.bean;

import java.io.Serializable;

import lombok.Data;

/**
 * 行程报告
 */
@Data
public class TripReport implements Serializable {

    //推荐车型ID，注意不是车型代码
    private String carModelId;

    //参与统计的最近行程次数
    private int totalTimes = 0;

    //总里程(km)
    private double totalMileage = 0;

    //总充电次数
    private int totalChargeTimes = 0;

    //碳减排(kg)
    private double saveCarbon = 0;

    //节省燃油费用(元)
    private double saveFuelAmount = 0;

    //单次最高里程(km)
    private double maxMileage = 0;

    //单次最高时长(h)
    private double maxDuration = 0;

    //最常用出发地
    private String mostFrequentStart;

    //最常用目的地
    private String mostFrequentDest;
}
