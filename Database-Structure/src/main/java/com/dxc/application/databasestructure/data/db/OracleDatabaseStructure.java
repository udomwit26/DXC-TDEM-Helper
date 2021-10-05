package com.dxc.application.databasestructure.data.db;

import com.dxc.application.databasestructure.model.DatabaseStructureModel;
import com.dxc.application.databasestructure.model.SequenceModel;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OracleDatabaseStructure {
    private final DataSource ds;

    @SneakyThrows
    public List<TableModel> listAllTables(String schema) {
        List<TableModel> tableList = new ArrayList<>();
        TableModel model = null;
        Connection con = ds.getConnection();
        DatabaseMetaData databaseMetaData = con.getMetaData();
        ResultSet resultSet = databaseMetaData.getTables(null, schema, null, new String[]{"TABLE"});
        while (resultSet.next()) {
            model = new TableModel();
            model.setPhysicalTableName(resultSet.getString("TABLE_NAME"));
            model.setLogicalTableName(makeTableLogicalName(model.getPhysicalTableName()));
            tableList.add(model);
        }
        resultSet.close();
        con.close();
        return tableList;
    }


    @SneakyThrows
    public List<DatabaseStructureModel> listColumnMetaData(String schema, String tableName) {
        List<DatabaseStructureModel> columnList = new ArrayList<>();
        List<DatabaseStructureModel> pkList = listTablePrimaryKey(schema, tableName);
        Connection con = ds.getConnection();
        DatabaseMetaData databaseMetaData = con.getMetaData();
        ResultSet columns = databaseMetaData.getColumns(null, schema, tableName, null);
        DatabaseStructureModel columnMeta = null;
        String columnName = null;
        while (columns.next()) {
            columnMeta = new DatabaseStructureModel();
            columnName = columns.getString("COLUMN_NAME");
            columnMeta.setColumnName(columnName);
            columnMeta.setLogicalName(makeColumnLogicalName(columnName));
            columnMeta.setDataType(columns.getString("TYPE_NAME"));
            columnMeta.setLen(columns.getInt("COLUMN_SIZE"));
            columnMeta.setPrec(columns.getInt("DECIMAL_DIGITS"));
            columnMeta.setPkSeq(chekAndReturnPKSeq(pkList, columnName));
            columnMeta.setMandatory(checkNullable(columnMeta.getPkSeq(), columns.getString("IS_NULLABLE")) ? "x" : "");
            columnList.add(columnMeta);
        }
        columns.close();
        con.close();
        return columnList;
    }

    @SneakyThrows
    public List<SequenceModel> listSequenceMetaData() {
        List<SequenceModel> sequenceList = new ArrayList<>();
        try (
                Connection con = ds.getConnection();
                PreparedStatement stmt = con.prepareStatement("SELECT SEQUENCE_NAME,MIN_VALUE,MAX_VALUE,LAST_NUMBER,INCREMENT_BY,CYCLE_FLAG FROM USER_SEQUENCES");
        ) {
            ResultSet sqRs = stmt.executeQuery();
            SequenceModel model = null;
            while (sqRs.next()) {
                model = new SequenceModel();
                model.setSequenceName(sqRs.getString("SEQUENCE_NAME"));
                model.setMinValue(sqRs.getBigDecimal("MIN_VALUE"));
                model.setMaxValue(sqRs.getBigDecimal("MAX_VALUE"));
                model.setLastNumber(sqRs.getBigDecimal("LAST_NUMBER"));
                model.setIncrementBy(sqRs.getBigDecimal("INCREMENT_BY"));
                model.setCycleFlag(sqRs.getString("CYCLE_FLAG"));
                sequenceList.add(model);
            }
            sqRs.close();
        }
        return sequenceList;
    }

    private boolean checkNullable(Integer pkSeq, String isNullable) {
        return pkSeq != null || StringUtils.equalsIgnoreCase(isNullable, "YES");
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
            dsModel.setPkSeq(pks.getInt("KEY_SEQ"));
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
    private Integer chekAndReturnPKSeq(List<DatabaseStructureModel> pkList, final String columnName) {
        Optional<DatabaseStructureModel> result = pkList.stream().filter(column -> column.getColumnName().equalsIgnoreCase(columnName)).findFirst();
        return result.map(DatabaseStructureModel::getPkSeq).orElse(null);
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
