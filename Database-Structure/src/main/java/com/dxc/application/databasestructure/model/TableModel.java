package com.dxc.application.databasestructure.model;

import lombok.Data;

@Data
public class TableModel {
    private String logicalTableName;
    private String physicalTableName;
}
