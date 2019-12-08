package com.powershare.etm.bean;

import java.io.Serializable;

import lombok.Data;

/**
 * 契合度
 */
@Data
public class MatchingDegree implements Serializable {
    //主键ID
    private String id;
    //用户ID
    private String userId;
    //车型ID
    private String carModelId;
    //契合度,百分比的分子取整
    private int degree = 0;
}
