package com.powershare.etm.bean;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 预测行程中的充电与耗电信息
 */
@Data
public class PredictCharge implements Serializable {

    //充电站坐标列表
    private List<Charge> chargeLocationList;

    //起点SOC,百分比分子取整
    private int startSoc = 100;

    //终点SOC,百分比分子取整
    private int destSoc = 0;

    //消耗电量(kwh)
    private double energy = 0;

    //充电成本(公),单位:元、RMB
    private double rmbPublich = 0;

    //充电成本(私),单位:元、RMB
    private double rmbPrivate = 0;
}
