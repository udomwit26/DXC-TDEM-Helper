package com.dxc.application.databasestructure.model;

import lombok.Data;

@Data
public class DatabaseStructureModel {
    private String columnName;
    private String dataType;
    private String len;
    private String prec;
    private String pk;
    private String mandatory;
}
