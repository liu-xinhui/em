package com.powershare.etm.util;

import com.powershare.etm.bean.CarModel;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 数据缓存
 */
public enum DataCache {
    INSTANCE;

    @Getter
    @Setter
    private List<CarModel> carModels;

    @Getter
    @Setter
    private int currentCarIndex;

    @Getter
    @Setter
    private int currentTemp;

    @Getter
    @Setter
    private int currentPowerWarn;
}