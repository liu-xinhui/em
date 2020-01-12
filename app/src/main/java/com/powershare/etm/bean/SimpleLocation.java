package com.powershare.etm.bean;

import java.io.Serializable;

import lombok.Data;

//充电站坐标
@Data
public class SimpleLocation implements Serializable {

    //普通充电站
    public static final int TYPE_NORMAL = 0;
    //常用充电站
    public static final int TYPE_ALWAYS = 10;

    //充电站ID
    private String id;

    //"充电站类型，0为普通充电站，10为经常使用的充电站"
    private int type = TYPE_NORMAL;

    //经度
    private double longitude;

    //纬度
    private double latitude;
}
