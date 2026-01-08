package com.warehouse.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Excel工具类，用于导入导出Excel文件
 */
public class ExcelUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 创建Excel工作簿
     */
    public static Workbook createWorkbook() {
        return new XSSFWorkbook();
    }

    /**
     * 创建表头行样式
     */
    public static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);

        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    /**
     * 创建数据行样式
     */
    public static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    /**
     * 设置单元格值
     */
    public static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }

        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(((LocalDateTime) value).format(DATE_TIME_FORMATTER));
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 获取单元格值
     */
    public static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 导出Excel
     *
     * @param dataList 数据列表
     * @param headers 表头数组
     * @param columnExtractors 列数据提取器可变参数
     * @return Excel文件字节数组
     */
    @SafeVarargs
    public static <T> byte[] exportExcel(List<T> dataList, String[] headers,
                                          Function<T, Object>... columnExtractors) throws IOException {
        Workbook workbook = createWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        // 创建表头
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 4000);
        }

        // 填充数据
        for (int i = 0; i < dataList.size(); i++) {
            Row row = sheet.createRow(i + 1);
            T data = dataList.get(i);

            for (int j = 0; j < columnExtractors.length; j++) {
                Cell cell = row.createCell(j);
                Object value = columnExtractors[j].apply(data);
                setCellValue(cell, value);
                cell.setCellStyle(dataStyle);
            }
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            // 设置最小宽度
            if (sheet.getColumnWidth(i) < 3000) {
                sheet.setColumnWidth(i, 3000);
            }
        }

        // 转换为字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * 导入Excel
     *
     * @param inputStream Excel文件输入流
     * @param startRow 开始行（0为第一行，通常设为1跳过表头）
     * @param rowMapper 行映射器
     * @return 数据列表
     */
    public static <T> List<T> importExcel(InputStream inputStream, int startRow,
                                          RowMapper<T> rowMapper) throws IOException {
        List<T> dataList = new ArrayList<>();

        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        int lastRowNum = sheet.getLastRowNum();
        for (int i = startRow; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            T data = rowMapper.mapRow(row, i);
            if (data != null) {
                dataList.add(data);
            }
        }

        workbook.close();
        return dataList;
    }

    /**
     * 行映射器接口
     */
    @FunctionalInterface
    public interface RowMapper<T> {
        T mapRow(Row row, int rowNum);
    }
}
