package com.dxc.application.databasestructure.service;

import com.dxc.application.databasestructure.data.db.OracleDatabaseStructure;
import com.dxc.application.databasestructure.model.DatabaseStructureModel;
import com.dxc.application.databasestructure.model.SequenceModel;
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
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OracleDatabaseStructureService implements DatabaseStructureService{
    private final OracleDatabaseStructure databaseStructure;
    private final ResourceLoader resourceLoader;

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
            List<SequenceModel> sequenceList = databaseStructure.listSequenceMetaData();
            createTableSheet(wb, tableList);
            createTableListSheet(wb, tableList);
            createSequenceSheet(wb, sequenceList);
            wb.removeSheetAt(3);
            evaluateFormulaCell(wb);
            wb.write(fos);
        }
    }
    @SneakyThrows
    private void createSequenceSheet(Workbook wb, final List<SequenceModel> sequenceList) {
        int startRow = 9;
        Sheet sequenceListSheet = wb.getSheetAt(1);
        for (SequenceModel model : sequenceList) {
            sequenceListSheet.getRow(startRow).getCell(1).setCellValue(model.getSequenceName());
            sequenceListSheet.getRow(startRow).getCell(18).setCellValue(model.getMinValue().intValue());
            sequenceListSheet.getRow(startRow).getCell(25).setCellValue(model.getMaxValue().intValue());
            sequenceListSheet.getRow(startRow).getCell(37).setCellValue(model.getLastNumber().intValue());
            sequenceListSheet.getRow(startRow).getCell(42).setCellValue(model.getIncrementBy().intValue());
            sequenceListSheet.getRow(startRow).getCell(50).setCellValue(model.getCycleFlag());
            startRow++;
        }
        for(int ii= 214;ii>startRow;ii--){
            sequenceListSheet.removeRow(sequenceListSheet.getRow(ii));
        }
    }

    @SneakyThrows
    private void createTableSheet(final Workbook wb, final List<TableModel> tableList) {
        CreationHelper createHelper = wb.getCreationHelper();
        Hyperlink link = null;
        int additionalSheet = 1;
        for (TableModel table : tableList) {
            Sheet dbSheet = wb.cloneSheet(3);
            wb.setSheetName(3 + additionalSheet, generateSheetName(table.getPhysicalTableName()));
            dbSheet.getRow(5).getCell(40).setCellValue(table.getPhysicalTableName());
            dbSheet.getRow(6).getCell(7).setCellValue(table.getLogicalTableName());

            // link
            link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
            link.setAddress("'Table List'!N3");
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
            generateLenValue(dbSheet.getRow(startRow).getCell(44),column.getDataType(),column.getLen());
            generatePrecValue(dbSheet.getRow(startRow).getCell(48),column.getDataType(),column.getPrec());
            if(column.getPkSeq()!=null){
                dbSheet.getRow(startRow).getCell(52).setCellValue(column.getPkSeq());
            }
            if(StringUtils.isNotBlank(column.getMandatory())){
                dbSheet.getRow(startRow).getCell(55).setCellValue(column.getMandatory());
            }
            startRow++;
        }
        if (startRow < 15) {
            for(int ii= 210;ii>15;ii--){
                dbSheet.removeRow(dbSheet.getRow(ii));
            }
        } else {
            for(int ii= 210;ii>startRow;ii--){
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
        Sheet tabListSheet = wb.getSheetAt(2);
        int startRow = 5;
        for (TableModel table : tableList) {
            tabListSheet.getRow(startRow).getCell(4).setCellValue(table.getLogicalTableName());
            tabListSheet.getRow(startRow).getCell(22).setCellValue(table.getPhysicalTableName());
            tabListSheet.getRow(startRow).getCell(40).setCellFormula(generateSheetName(table.getPhysicalTableName())+"!BP6");
            tabListSheet.getRow(startRow).getCell(47).setCellFormula(generateSheetName(table.getPhysicalTableName())+"!CF6");

            link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
            link.setAddress(generateSheetName(table.getPhysicalTableName())+"!H7");
            tabListSheet.getRow(startRow).getCell(4).setHyperlink(link);

            startRow++;
        }
        for(int ii= 211;ii>startRow;ii--){
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
        if (StringUtils.equalsIgnoreCase(dataType, "NUMBER")) {
            cell.setCellValue(precValue);
        }
    }

    @SneakyThrows
    private void generateLenValue(Cell cell, String dataType, Integer lenValue) {
        if (StringUtils.equalsIgnoreCase(dataType, "NUMBER")
                || StringUtils.equalsIgnoreCase(dataType, "CHAR")
                || StringUtils.equalsIgnoreCase(dataType, "VARCHAR2")
                || StringUtils.equalsIgnoreCase(dataType, "NCHAR")
                || StringUtils.equalsIgnoreCase(dataType, "NVARCHAR2")) {
            cell.setCellValue(lenValue);
        }
    }
}
