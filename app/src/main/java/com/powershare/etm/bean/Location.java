package com.powershare.etm.bean;

import lombok.Data;

/**
 * 位置坐标点
 */
@Data
public class Location {
    //经度
    private double longitude = 0;
    //纬度
    private double latitude = 0;
}
