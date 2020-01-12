package com.powershare.etm.bean;

import android.text.TextUtils;

import java.io.Serializable;

import lombok.Data;

@Data
public class Charge implements Serializable {
    //经度
    private double longitude = 0;

    //纬度
    private double latitude = 0;

    //充电站ID
    private String id;

    //充电站名称
    private String name;

    //充电站地址
    private String address;

    //电价
    private String price;

    private String logoUrl;

    private int dcTotalAvailableNum;
    private int dcTotalNum;
    private int acTotalAvailableNum;
    private int acTotalNum;
}
