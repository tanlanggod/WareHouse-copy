package com.warehouse.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF工具类，用于生成PDF报表
 */
public class PdfUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static BaseFont CHINESE_FONT;

    static {
        try {
            // 使用itext-asian的中文字体
            CHINESE_FONT = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建中文字体
     */
    public static Font createFont(int size, int style) {
        return new Font(CHINESE_FONT, size, style);
    }

    /**
     * 创建标题字体
     */
    public static Font createTitleFont() {
        return createFont(18, Font.BOLD);
    }

    /**
     * 创建副标题字体
     */
    public static Font createSubTitleFont() {
        return createFont(12, Font.NORMAL);
    }

    /**
     * 创建表格字体
     */
    public static Font createTableFont() {
        return createFont(10, Font.NORMAL);
    }

    /**
     * 创建表头字体
     */
    public static Font createTableHeaderFont() {
        return createFont(10, Font.BOLD);
    }

    /**
     * 创建PDF文档
     */
    public static Document createDocument() {
        return new Document(PageSize.A4, 50, 50, 50, 50);
    }

    /**
     * 添加标题
     */
    public static void addTitle(Document document, String title) throws DocumentException {
        Paragraph titleParagraph = new Paragraph(title, createTitleFont());
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        titleParagraph.setSpacingAfter(20);
        document.add(titleParagraph);
    }

    /**
     * 添加副标题
     */
    public static void addSubTitle(Document document, String subtitle) throws DocumentException {
        Paragraph subTitleParagraph = new Paragraph(subtitle, createSubTitleFont());
        subTitleParagraph.setAlignment(Element.ALIGN_CENTER);
        subTitleParagraph.setSpacingAfter(20);
        document.add(subTitleParagraph);
    }

    /**
     * 创建表格
     */
    public static PdfPTable createTable(int columnCount) {
        PdfPTable table = new PdfPTable(columnCount);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        return table;
    }

    /**
     * 添加表头单元格
     */
    public static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, createTableHeaderFont()));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }

    /**
     * 添加普通单元格
     */
    public static void addCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, createTableFont()));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }

    /**
     * 添加左对齐单元格
     */
    public static void addLeftAlignCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, createTableFont()));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }

    /**
     * 生成PDF字节数组
     */
    public static byte[] generatePdf(PdfGenerator generator) throws DocumentException, IOException {
        Document document = createDocument();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, outputStream);
        document.open();

        generator.generate(document);

        document.close();
        return outputStream.toByteArray();
    }

    /**
     * PDF生成器接口
     */
    @FunctionalInterface
    public interface PdfGenerator {
        void generate(Document document) throws DocumentException;
    }
}
