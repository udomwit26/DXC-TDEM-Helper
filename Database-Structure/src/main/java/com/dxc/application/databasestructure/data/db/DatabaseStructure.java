package com.dxc.application.databasestructure.data.db;

import com.dxc.application.databasestructure.model.DatabaseStructureModel;
import com.dxc.application.databasestructure.model.TableModel;
import com.google.common.base.CaseFormat;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseStructure {
    private final DataSource ds;

    @SneakyThrows
    public List<TableModel> listAllTables(String schema) {
        List<TableModel> tableList = new ArrayList<>();
        TableModel model = null;
        Connection con = ds.getConnection();
        DatabaseMetaData databaseMetaData = con.getMetaData();
        ResultSet resultSet = databaseMetaData.getTables(schema, null, null, new String[]{"TABLE"});
        while (resultSet.next()) {
            model = new TableModel();
            model.setPhysicalTableName(resultSet.getString("TABLE_NAME"));
            model.setLogicalTableName(makeTableLogicalName(model.getPhysicalTableName()));
            tableList.add(model);
        }
        resultSet.close();
        con.close();
        return tableList.stream().filter(
                tableName -> tableName.getPhysicalTableName().startsWith("TB_") && !tableName.getPhysicalTableName().startsWith("TB_T")
        ).collect(Collectors.toList());
    }


    @SneakyThrows
    public List<DatabaseStructureModel> listColumnMetaData(String schema, String tableName) {
        List<DatabaseStructureModel> columnList = new ArrayList<>();
        List<DatabaseStructureModel> pkList = listTablePrimaryKey(schema, tableName);
        Connection con = ds.getConnection();
        DatabaseMetaData databaseMetaData = con.getMetaData();
        ResultSet columns = databaseMetaData.getColumns(schema, null, tableName, null);
        DatabaseStructureModel columnMeta = null;
        String columnName = null;
        while (columns.next()) {
            columnMeta = new DatabaseStructureModel();
            columnName = columns.getString("COLUMN_NAME");
            columnMeta.setColumnName(columnName);
            columnMeta.setLogicalName(makeColumnLogicalName(columnName));
            columnMeta.setDataType(columns.getString("TYPE_NAME"));
            columnMeta.setLen(columns.getString("COLUMN_SIZE"));
            columnMeta.setPrec(columns.getString("DECIMAL_DIGITS"));
            columnMeta.setPkSeq(chekAndReturnPKSeq(pkList, columnName));
            columnMeta.setMandatory(checkNullable(columnMeta.getPkSeq(), columns.getString("IS_NULLABLE")) ? "x" : "");
            columnList.add(columnMeta);
        }
        columns.close();
        con.close();
        return columnList;
    }

    private boolean checkNullable(String pkSeq, String isNullable) {
        return StringUtils.isNotBlank(pkSeq) || StringUtils.equalsIgnoreCase(isNullable, "YES");
    }

    @SneakyThrows
    private List<DatabaseStructureModel> listTablePrimaryKey(String schema, String tableName) {
        List<DatabaseStructureModel> pkList = new ArrayList<>();
        Connection con = ds.getConnection();
        DatabaseMetaData databaseMetaData = con.getMetaData();
        ResultSet pks = databaseMetaData.getPrimaryKeys(schema, null, tableName);
        DatabaseStructureModel dsModel = null;
        while (pks.next()) {
            dsModel = new DatabaseStructureModel();
            dsModel.setColumnName(pks.getString("COLUMN_NAME"));
            dsModel.setPkSeq(pks.getString("KEY_SEQ"));
            pkList.add(dsModel);
        }
        pks.close();
        con.close();
        return pkList;
    }

    @SneakyThrows
    private String makeColumnLogicalName(String columnName) {
        String camelCase = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, columnName);
        String[] splitCamel = StringUtils.splitByCharacterTypeCamelCase(camelCase);
        return StringUtils.join(splitCamel, " ");
    }

    @SneakyThrows
    private String chekAndReturnPKSeq(List<DatabaseStructureModel> pkList, final String columnName) {
        Optional<DatabaseStructureModel> result = pkList.stream().filter(column -> column.getColumnName().equalsIgnoreCase(columnName)).findFirst();
        if (result.isPresent()) {
            return result.get().getPkSeq();
        } else {
            return "";
        }
    }

    @SneakyThrows
    private String makeTableLogicalName(String tableName) {
        String tableType = StringUtils.substring(tableName, 3, 4);
        String tlName = StringUtils.substring(tableName, 5);
        String camelCase = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tlName);
        String[] splitCamel = StringUtils.splitByCharacterTypeCamelCase(camelCase);
        String tlNameInHuman = StringUtils.join(splitCamel, " ");
        String logicalName = null;
        switch (tableType) {
            case "S":
                logicalName = tlNameInHuman + " Staging";
                break;
            case "T":
                logicalName = tlNameInHuman + " Temporary";
                break;
            case "M":
                logicalName = tlNameInHuman + " Master";
                break;
            case "R":
                logicalName = tlNameInHuman + " Transaction";
                break;
            case "H":
                logicalName = tlNameInHuman + " History";
                break;
            case "L":
                logicalName = tlNameInHuman + " Monitoring";
                break;
            default:
                logicalName = tlNameInHuman;
        }
        return logicalName;
    }
}
