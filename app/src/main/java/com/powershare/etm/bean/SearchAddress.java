package com.powershare.etm.bean;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchAddress implements Serializable {
    private String name;
    private String address;
}
