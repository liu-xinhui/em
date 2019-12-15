package com.powershare.etm.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChargeWarn {
    private TripPoint tripPoint;
    private TripSoc tripSoc;
}
