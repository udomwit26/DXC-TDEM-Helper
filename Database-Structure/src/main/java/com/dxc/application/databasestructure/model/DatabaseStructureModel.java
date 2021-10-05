package com.dxc.application.databasestructure.model;

import lombok.Data;

@Data
public class DatabaseStructureModel {
    private String columnName;
    private String LogicalName;
    private String dataType;
    private Integer len;
    private Integer prec;
    private Integer pkSeq;
    private String mandatory;
}
