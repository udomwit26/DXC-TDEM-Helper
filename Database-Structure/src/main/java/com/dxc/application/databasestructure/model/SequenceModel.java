package com.dxc.application.databasestructure.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SequenceModel {
    private String sequenceName;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private BigDecimal lastNumber;
    private BigDecimal incrementBy;
    private String cycleFlag;
}
