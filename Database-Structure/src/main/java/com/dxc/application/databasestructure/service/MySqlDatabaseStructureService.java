package com.dxc.application.databasestructure.service;

import com.dxc.application.databasestructure.data.db.MySQLDatabaseStructure;
import com.dxc.application.databasestructure.model.DatabaseStructureModel;
import com.dxc.application.databasestructure.model.TableModel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MySqlDatabaseStructureService implements DatabaseStructureService {
    private final MySQLDatabaseStructure databaseStructure;

    @Value("${output.path}")
    private String outputFolder;
    @Value("${output.fileName}")
    private String outputFileName;

    @Value("${db.brand}")
    private String dbBrand;
    @Value("${db.schema}")
    private String schemaName;

    @SneakyThrows
    @Override
    public void createExcel() {
        String templateFileName = dbBrand + "_Database_Structure.xlsx";
        String tempOutFileName = outputFolder + outputFileName;
        File outFile = new File(tempOutFileName);

        try (InputStream fis = new ClassPathResource("template/" + templateFileName).getInputStream();
             FileOutputStream fos = new FileOutputStream(outFile);
             Workbook wb = new XSSFWorkbook(fis);
        ) {
            List<TableModel> tableList = databaseStructure.listAllTables(schemaName);
            createTableSheet(wb, tableList);
            createTableListSheet(wb, tableList);
            wb.removeSheetAt(2);
            evaluateFormulaCell(wb);
            wb.write(fos);
        }
    }

    @SneakyThrows
    private void createTableSheet(final Workbook wb, final List<TableModel> tableList) {
        CreationHelper createHelper = wb.getCreationHelper();
        Hyperlink link = null;
        int additionalSheet = 1;
        for (TableModel table : tableList) {
            Sheet dbSheet = wb.cloneSheet(2);
            wb.setSheetName(2 + additionalSheet, generateSheetName(table.getPhysicalTableName()));
            dbSheet.getRow(5).getCell(40).setCellValue(table.getPhysicalTableName());
            dbSheet.getRow(6).getCell(7).setCellValue(table.getLogicalTableName());

            // link
            link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
            link.setAddress("'Table List'!M3");
            dbSheet.getRow(6).getCell(7).setHyperlink(link);

            setDbSheetInfo(dbSheet, table.getPhysicalTableName());
            additionalSheet++;
        }
    }

    @SneakyThrows
    private void setDbSheetInfo(Sheet dbSheet, String tableName) {
        List<DatabaseStructureModel> columns = databaseStructure.listColumnMetaData(schemaName, tableName);
        int startRow = 9;
        for (DatabaseStructureModel column : columns) {
            dbSheet.getRow(startRow).getCell(4).setCellValue(column.getColumnName());
            dbSheet.getRow(startRow).getCell(21).setCellValue(column.getLogicalName());
            dbSheet.getRow(startRow).getCell(38).setCellValue(column.getDataType());
            generateLenValue(dbSheet.getRow(startRow).getCell(44), column.getDataType(), column.getLen());
            generatePrecValue(dbSheet.getRow(startRow).getCell(48), column.getDataType(), column.getPrec());
            if (column.getPkSeq() != null) {
                dbSheet.getRow(startRow).getCell(52).setCellValue(column.getPkSeq());
            }
            if (StringUtils.isNotBlank(column.getMandatory())) {
                dbSheet.getRow(startRow).getCell(55).setCellValue(column.getMandatory());
            }
            startRow++;
        }
        if (startRow < 17) {
            for (int ii = 209; ii > 17; ii--) {
                dbSheet.removeRow(dbSheet.getRow(ii));
            }
        } else {
            for (int ii = 209; ii > startRow; ii--) {
                dbSheet.removeRow(dbSheet.getRow(ii));
            }
        }
    }

    @SneakyThrows
    private String generateSheetName(String sheetName) {
        if (sheetName.length() > 31) {
            return StringUtils.substring(sheetName, 0, 31);
        } else {
            return sheetName;
        }
    }

    @SneakyThrows
    private void createTableListSheet(final Workbook wb, final List<TableModel> tableList) {
        CreationHelper createHelper = wb.getCreationHelper();
        Hyperlink link = null;
        Sheet tabListSheet = wb.getSheet("Table List");
        int startRow = 5;
        for (TableModel table : tableList) {
            tabListSheet.getRow(startRow).getCell(3).setCellValue(table.getLogicalTableName());
            tabListSheet.getRow(startRow).getCell(21).setCellValue(table.getPhysicalTableName());
            tabListSheet.getRow(startRow).getCell(39).setCellFormula(generateSheetName(table.getPhysicalTableName()) + "!BP6");
            tabListSheet.getRow(startRow).getCell(46).setCellFormula(generateSheetName(table.getPhysicalTableName()) + "!CF6");

            // link
            link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
            link.setAddress(generateSheetName(table.getPhysicalTableName()) + "!H7");
            tabListSheet.getRow(startRow).getCell(3).setHyperlink(link);

            startRow++;
        }
        for (int ii = 204; ii > startRow; ii--) {
            tabListSheet.removeRow(tabListSheet.getRow(ii));
        }
    }


    @SneakyThrows
    private void evaluateFormulaCell(Workbook wb) {
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        for (Sheet sheet : wb) {
            for (Row r : sheet) {
                for (Cell c : r) {
                    if (c.getCellType() == CellType.FORMULA) {
                        evaluator.evaluateFormulaCell(c);
                    }
                }
            }
        }
    }

    @SneakyThrows
    private void generatePrecValue(Cell cell, String dataType, Integer precValue) {
        if (StringUtils.equalsIgnoreCase(dataType, "DECIMAL")) {
            cell.setCellValue(precValue);
        }
    }

    @SneakyThrows
    private void generateLenValue(Cell cell, String dataType, Integer lenValue) {
        if (StringUtils.equalsIgnoreCase(dataType, "DECIMAL")
                || StringUtils.equalsIgnoreCase(dataType, "CHAR")
                || StringUtils.equalsIgnoreCase(dataType, "VARCHAR")
                || StringUtils.equalsIgnoreCase(dataType, "LONGBLOB")) {
            cell.setCellValue(lenValue);
        }
    }
}
