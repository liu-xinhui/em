package com.powershare.etm.bean;

import lombok.Data;

/**
 * 车辆行程中的实时SOC和电量消耗
 */
@Data
public class TripSoc {
    //SOC,百分比分子取整
    private int soc = 100;

    //已完成充电次数(次)
    private int chargeTimes = 0;

    //消耗电量(kwh)
    private double energy = 0;

    //充电成本(公),单位:元、RMB
    private double rmbPublich = 0;

    //充电成本(私),单位:元、RMB
    private double rmbPrivate = 0;
}
