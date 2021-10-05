package com.dxc.application.databasestructure.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class POIExcelUtil {

    public static void copyViewSetup(Sheet source, Sheet destination) {
        destination.setDisplayGridlines(source.isDisplayGridlines());
        destination.setPrintGridlines(source.isPrintGridlines());
    }

    public static void copyPrintSetup(PrintSetup source, PrintSetup destination) {
        destination.setCopies(source.getCopies());
        destination.setDraft(source.getDraft());
        destination.setFitHeight(source.getFitHeight());
        destination.setFitWidth(source.getFitWidth());
        destination.setFooterMargin(source.getFooterMargin());
        destination.setHeaderMargin(source.getHeaderMargin());
        destination.setHResolution(source.getHResolution());
        destination.setLandscape(source.getLandscape());
        destination.setLeftToRight(source.getLeftToRight());
        destination.setNoColor(source.getNoColor());
        destination.setNoOrientation(source.getNoOrientation());
        destination.setNotes(source.getNotes());
        destination.setPageStart(source.getPageStart());
        destination.setPaperSize(source.getPaperSize());
        destination.setScale(source.getScale());
        destination.setUsePage(source.getUsePage());
        destination.setValidSettings(source.getValidSettings());
        destination.setVResolution(source.getVResolution());
    }

    public static void merge(List list, Sheet sheet, int columnIndex, boolean isClearingList) {
        CellRangeAddress newCellRangeAddress = new CellRangeAddress((int) list.get(0), (int) list.get(list.size() - 1), columnIndex, columnIndex);
        sheet.addMergedRegion(newCellRangeAddress);
        // clear index temp
        if (isClearingList) {
            list.clear();
        }
    }

    public static void merge(Sheet sheet, int startRowIndex, int endRowIndex, int startColumnIndex, int endColumnIndex) {
        CellRangeAddress newCellRangeAddress = new CellRangeAddress(startRowIndex, endRowIndex, startColumnIndex, endColumnIndex);
        sheet.addMergedRegion(newCellRangeAddress);
    }

    public static void copyRow(Sheet sourceSheet, Sheet destinationSheet, int sourceRowNum, int destinationRowNum) {
        copyRow(sourceSheet, destinationSheet, sourceRowNum, destinationRowNum, false);
    }

    public static void copyRow(Sheet sourceSheet, Sheet destinationSheet, int sourceRowNum, int destinationRowNum, boolean isFreezingHeight) {
        // Get the source / new row
        Row newRow = destinationSheet.getRow(destinationRowNum);
        Row sourceRow = sourceSheet.getRow(sourceRowNum);

        if (sourceRow != null) {
            // If the row exist in destination, push down all rows by 1 else create a new row
            if (newRow == null) {
                newRow = destinationSheet.createRow(destinationRowNum);
            }

            // Loop through source columns to add to new row
            for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
                // Grab a copy of the old/new cell
                Cell oldCell = sourceRow.getCell(i);
                Cell newCell = newRow.createCell(i);

                // If the old cell is null jump to next cell
                if (oldCell == null) {
                    newCell = null;
                    continue;
                }

                copyCell(oldCell, newCell, isFreezingHeight);
            }

            // If there are any merged regions in the source row, copy to new row
            for (int i = 0; i < sourceSheet.getNumMergedRegions(); i++) {
                CellRangeAddress cellRangeAddress = sourceSheet.getMergedRegion(i);
                if (cellRangeAddress.getFirstRow() == sourceRow.getRowNum()) {
                    CellRangeAddress newCellRangeAddress = new CellRangeAddress(newRow.getRowNum(), (newRow.getRowNum() + (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow())), cellRangeAddress.getFirstColumn(), cellRangeAddress.getLastColumn());
                    destinationSheet.addMergedRegion(newCellRangeAddress);
                }
            }
        }
    }

    public static void copyXSSFRow(XSSFWorkbook workbook, XSSFSheet sourceSheet, XSSFSheet destinationSheet, int sourceRowNum, int destinationRowNum, boolean isFreezingHeight) {
        // Get the source / new row
        XSSFRow newRow = destinationSheet.getRow(destinationRowNum);
        XSSFRow sourceRow = sourceSheet.getRow(sourceRowNum);

        if (sourceRow != null) {
            // If the row exist in destination, push down all rows by 1 else create a new row
            if (newRow == null) {
                newRow = destinationSheet.createRow(destinationRowNum);
            }

            // Loop through source columns to add to new row
            for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
                // Grab a copy of the old/new cell
                XSSFCell oldCell = sourceRow.getCell(i);
                XSSFCell newCell = newRow.createCell(i);

                // If the old cell is null jump to next cell
                if (oldCell == null) {
                    newCell = null;
                    continue;
                }

                copyXSSFCell(workbook, oldCell, newCell, isFreezingHeight);
            }

            // If there are any merged regions in the source row, copy to new row
            for (int i = 0; i < sourceSheet.getNumMergedRegions(); i++) {
                CellRangeAddress cellRangeAddress = sourceSheet.getMergedRegion(i);
                if (cellRangeAddress.getFirstRow() == sourceRow.getRowNum()) {
                    CellRangeAddress newCellRangeAddress = new CellRangeAddress(newRow.getRowNum(), (newRow.getRowNum() + (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow())), cellRangeAddress.getFirstColumn(), cellRangeAddress.getLastColumn());
                    destinationSheet.addMergedRegion(newCellRangeAddress);
                }
            }
        }
    }

    public static void copyCell(Cell oldCell, Cell newCell) {
        copyCell(oldCell, newCell, true);
    }

    public static void copyCell(Cell oldCell, Cell newCell, boolean isFreezingHeight) {
        copyCell(oldCell, newCell, isFreezingHeight, new HashMap<>());
    }

    public static void copyXSSFCell(XSSFWorkbook workbook, XSSFCell oldCell, XSSFCell newCell, boolean isFreezingHeight) {
        copyXSSFCell(workbook, oldCell, newCell, isFreezingHeight, new HashMap<>());
    }

    public static void copyCell(Cell oldCell, Cell newCell, boolean isFreezingHeight, Map<Integer, CellStyle> styleMap) {
        if (styleMap != null) {
            if (oldCell.getSheet().getWorkbook() == newCell.getSheet().getWorkbook()) {
                newCell.setCellStyle(oldCell.getCellStyle());
            } else {
                int stHashCode = oldCell.getCellStyle().hashCode();
                CellStyle newCellStyle = styleMap.get(stHashCode);
                if (newCellStyle == null) {
                    newCellStyle = newCell.getRow().getSheet().getWorkbook().createCellStyle();
                    newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
                    styleMap.put(stHashCode, newCellStyle);
                }
                newCell.setCellStyle(newCellStyle);
            }
        }

        // If there is a cell comment, copy
        if (oldCell.getCellComment() != null) {
            newCell.setCellComment(oldCell.getCellComment());
        }

        // If there is a cell hyperlink, copy
        if (oldCell.getHyperlink() != null) {
            newCell.setHyperlink(oldCell.getHyperlink());
        }

        // copy height
        if (oldCell.getRow().getZeroHeight()) {
            newCell.getRow().setZeroHeight(true); // hide it
        } else if (isFreezingHeight) {
            newCell.getRow().setHeight(oldCell.getRow().getHeight());
        }
        // copy width
        if (oldCell.getSheet().isColumnHidden(oldCell.getColumnIndex())) {
            newCell.getSheet().setColumnHidden(newCell.getColumnIndex(), true);
        } else {
            newCell.getSheet().setColumnWidth(newCell.getColumnIndex(), oldCell.getSheet().getColumnWidth(oldCell.getColumnIndex()));
        }

        // Set the cell data value
        switch (oldCell.getCellType()) {
            case BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
                break;
            case FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            case NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case STRING:
                newCell.setCellValue(oldCell.getRichStringCellValue());
                break;
        }

    }

    public static void copyXSSFCell(XSSFWorkbook workbook, XSSFCell oldCell, XSSFCell newCell, boolean isFreezingHeight, Map<Integer, XSSFCellStyle> styleMap) {
        if (styleMap != null) {
            if (oldCell.getSheet().getWorkbook() == newCell.getSheet().getWorkbook()) {
                newCell.setCellStyle(oldCell.getCellStyle());
            } else {
                int stHashCode = oldCell.getCellStyle().hashCode();
                XSSFCellStyle newCellStyle = styleMap.get(stHashCode);
                if (newCellStyle == null) {
                    newCellStyle = newCell.getRow().getSheet().getWorkbook().createCellStyle();
                    newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
                    styleMap.put(stHashCode, newCellStyle);
                }
                newCell.setCellStyle(newCellStyle);
            }
        }

        // If there is a cell comment, copy
        if (oldCell.getCellComment() != null) {
            newCell.setCellComment(oldCell.getCellComment());
        }

        // If there is a cell hyperlink, copy
        if (oldCell.getHyperlink() != null) {
            newCell.setHyperlink(oldCell.getHyperlink());
        }

        // copy height
        if (oldCell.getRow().getZeroHeight()) {
            newCell.getRow().setZeroHeight(true); // hide it
        } else if (isFreezingHeight) {
            newCell.getRow().setHeight(oldCell.getRow().getHeight());
        }
        // copy width
        if (oldCell.getSheet().isColumnHidden(oldCell.getColumnIndex())) {
            newCell.getSheet().setColumnHidden(newCell.getColumnIndex(), true);
        } else {
            newCell.getSheet().setColumnWidth(newCell.getColumnIndex(), oldCell.getSheet().getColumnWidth(oldCell.getColumnIndex()));
        }

        // Set the cell data type
        newCell.setCellType(oldCell.getCellType());

        // Set the cell data value
        switch (oldCell.getCellType()) {
            case BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
                break;
            case FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            case NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case STRING:
                newCell.setCellValue(oldCell.getRichStringCellValue());
                break;
        }

    }

    public static void copySection(Sheet sourceSheet, Sheet destinationSheet, int sourceStartRow, int sourceStartCol, int sourceEndRow, int sourceEndCol, int desRow, int desCol) {
        copySection(sourceSheet, destinationSheet, sourceStartRow, sourceStartCol, sourceEndRow, sourceEndCol, desRow, desCol, false);
    }


    public static void copySection(Sheet sourceSheet, Sheet destinationSheet, int sourceStartRow, int sourceStartCol, int sourceEndRow, int sourceEndCol, int desRow, int desCol, boolean isFreezingHeight) {
        copySection(sourceSheet, destinationSheet, sourceStartRow, sourceStartCol, sourceEndRow, sourceEndCol, desRow, desCol, isFreezingHeight, true);

    }

    public static void copySection(Sheet sourceSheet, Sheet destinationSheet, int sourceStartRow, int sourceStartCol, int sourceEndRow, int sourceEndCol, int desRow, int desCol, boolean isFreezingHeight, boolean isCopyStyle) {
        Row newRow = null;
        Row sourceRow = null;
        Cell oldCell = null;
        Cell newCell = null;
        int desRowCount = 0;
        int desCellCount = 0;
        Map<Integer, CellStyle> stypeMap = isCopyStyle ? new HashMap<>() : null;

        for (int rowI = sourceStartRow; rowI <= sourceEndRow; rowI++) {
            desCellCount = 0;

            newRow = destinationSheet.getRow(desRow + desRowCount);
            sourceRow = sourceSheet.getRow(rowI);

            // If the row exist in destination, push down all rows by 1 else create a new row
            if (newRow == null) {
                newRow = destinationSheet.createRow(desRow + desRowCount);
            }

            for (int colI = sourceStartCol; colI <= sourceEndCol; colI++) {
                if (sourceRow != null && newRow != null) {
                    oldCell = sourceRow.getCell(colI);
                    newCell = newRow.createCell(desCol + desCellCount);
                }

                desCellCount++;

                // If the old cell is null jump to next cell
                if (oldCell == null) {
                    newCell = null;
                    continue;
                }

                copyCell(oldCell, newCell, isFreezingHeight, stypeMap);
            }
            desRowCount++;
        }

        // If there are any merged regions in the source row, copy to destination
        for (int i = 0; i < sourceSheet.getNumMergedRegions(); i++) {
            CellRangeAddress cellRangeAddress = sourceSheet.getMergedRegion(i);

            if (cellRangeAddress.getFirstRow() >= sourceStartRow && cellRangeAddress.getLastRow() <= sourceEndRow && cellRangeAddress.getFirstColumn() >= sourceStartCol && cellRangeAddress.getLastColumn() <= sourceEndCol) {
                CellRangeAddress newCellRangeAddress = new CellRangeAddress(desRow + (cellRangeAddress.getFirstRow() - sourceStartRow), ((desRow + (cellRangeAddress.getFirstRow() - sourceStartRow)) + (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow())), desCol + (cellRangeAddress.getFirstColumn() - sourceStartCol), ((desCol + (cellRangeAddress.getFirstColumn() - sourceStartCol)) + (cellRangeAddress.getLastColumn() - cellRangeAddress.getFirstColumn())));
                destinationSheet.addMergedRegion(newCellRangeAddress);
            }
        }
    }

    public static void copySheet(Sheet sourceSheet, Sheet desSheet, boolean isCopyStyle) {
        int rowNum = sourceSheet.getLastRowNum();
        for (int i = 0; i <= rowNum; i++) {
            copyRow(sourceSheet, desSheet, i, i, true);
        }
    }

    public static void copyXSSFSheet(XSSFSheet sourceSheet, XSSFSheet desSheet, boolean isCopyStyle) {
        int rowNum = sourceSheet.getLastRowNum();
        for (int i = 0; i <= rowNum; i++) {
            copyXSSFRow(desSheet.getWorkbook(), sourceSheet, desSheet, i, i, true);
        }
    }

}
