package com.warehouse.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.warehouse.entity.Inbound;
import com.warehouse.entity.Outbound;
import com.warehouse.entity.Product;
import com.warehouse.repository.InboundRepository;
import com.warehouse.repository.OutboundRepository;
import com.warehouse.repository.ProductRepository;
import com.warehouse.util.PdfUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 报表服务，负责生成各类PDF报表
 */
@Service
public class ReportService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InboundRepository inboundRepository;

    @Autowired
    private OutboundRepository outboundRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 生成库存报表PDF
     */
    public byte[] generateStockReport() throws DocumentException, IOException {
        return PdfUtil.generatePdf(document -> {
            // 添加标题
            PdfUtil.addTitle(document, "库存报表");
            PdfUtil.addSubTitle(document, "生成时间：" + LocalDateTime.now().format(DATE_TIME_FORMATTER));

            // 获取所有商品
            List<Product> products = productRepository.findAll();

            // 创建表格
            PdfPTable table = PdfUtil.createTable(8);
            try {
                table.setWidths(new float[]{1, 2, 2, 2, 2, 1.5f, 1.5f, 1.5f});
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }

            // 添加表头
            PdfUtil.addHeaderCell(table, "ID");
            PdfUtil.addHeaderCell(table, "商品编号");
            PdfUtil.addHeaderCell(table, "商品名称");
            PdfUtil.addHeaderCell(table, "分类");
            PdfUtil.addHeaderCell(table, "供应商");
            PdfUtil.addHeaderCell(table, "单价");
            PdfUtil.addHeaderCell(table, "库存");
            PdfUtil.addHeaderCell(table, "最低库存");

            // 添加数据
            int totalStock = 0;
            int lowStockCount = 0;

            for (Product product : products) {
                PdfUtil.addCell(table, String.valueOf(product.getId()));
                PdfUtil.addCell(table, product.getCode() != null ? product.getCode() : "");
                PdfUtil.addLeftAlignCell(table, product.getName() != null ? product.getName() : "");
                PdfUtil.addCell(table, product.getCategory() != null ? product.getCategory().getName() : "");
                PdfUtil.addCell(table, product.getSupplier() != null ? product.getSupplier().getName() : "");
                PdfUtil.addCell(table, product.getPrice() != null ? product.getPrice().toString() : "0");
                PdfUtil.addCell(table, String.valueOf(product.getStockQty() != null ? product.getStockQty() : 0));
                PdfUtil.addCell(table, String.valueOf(product.getMinStock() != null ? product.getMinStock() : 0));

                totalStock += product.getStockQty() != null ? product.getStockQty() : 0;
                if (product.getStockQty() != null && product.getMinStock() != null && product.getStockQty() <= product.getMinStock()) {
                    lowStockCount++;
                }
            }

            document.add(table);

            // 添加统计信息
            Paragraph summary = new Paragraph("\n统计信息：", PdfUtil.createSubTitleFont());
            document.add(summary);

            Paragraph totalProducts = new Paragraph("商品总数：" + products.size(), PdfUtil.createTableFont());
            totalProducts.setSpacingBefore(10);
            document.add(totalProducts);

            Paragraph totalStockPara = new Paragraph("总库存量：" + totalStock, PdfUtil.createTableFont());
            document.add(totalStockPara);

            Paragraph lowStockPara = new Paragraph("低库存商品数：" + lowStockCount, PdfUtil.createTableFont());
            document.add(lowStockPara);
        });
    }

    /**
     * 生成入库统计报表PDF
     */
    public byte[] generateInboundReport(LocalDateTime startDate, LocalDateTime endDate) throws DocumentException, IOException {
        Document document = PdfUtil.createDocument();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // 添加标题
        PdfUtil.addTitle(document, "入库统计报表");

        String dateRange;
        if (startDate != null && endDate != null) {
            dateRange = startDate.format(DATE_FORMATTER) + " 至 " + endDate.format(DATE_FORMATTER);
        } else {
            dateRange = "全部";
        }
        PdfUtil.addSubTitle(document, "统计期间：" + dateRange);
        PdfUtil.addSubTitle(document, "生成时间：" + LocalDateTime.now().format(DATE_TIME_FORMATTER));

        // 获取入库记录
        List<Inbound> inbounds;
        if (startDate != null && endDate != null) {
            inbounds = inboundRepository.findByInboundDateBetween(startDate, endDate);
        } else {
            inbounds = inboundRepository.findAll();
        }

        // 创建表格
        PdfPTable table = PdfUtil.createTable(6);
        table.setWidths(new float[]{2, 2, 3, 1.5f, 2, 3});

        // 添加表头
        PdfUtil.addHeaderCell(table, "入库单号");
        PdfUtil.addHeaderCell(table, "商品编号");
        PdfUtil.addHeaderCell(table, "商品名称");
        PdfUtil.addHeaderCell(table, "数量");
        PdfUtil.addHeaderCell(table, "入库日期");
        PdfUtil.addHeaderCell(table, "供应商");

        // 添加数据
        int totalQuantity = 0;
        for (Inbound inbound : inbounds) {
            PdfUtil.addCell(table, inbound.getInboundNo() != null ? inbound.getInboundNo() : "");
            PdfUtil.addCell(table, inbound.getProduct() != null ? inbound.getProduct().getCode() : "");
            PdfUtil.addLeftAlignCell(table, inbound.getProduct() != null ? inbound.getProduct().getName() : "");
            PdfUtil.addCell(table, String.valueOf(inbound.getQuantity()));
            PdfUtil.addCell(table, inbound.getInboundDate() != null ? inbound.getInboundDate().format(DATE_FORMATTER) : "");
            PdfUtil.addCell(table, inbound.getSupplier() != null ? inbound.getSupplier().getName() : "");
            totalQuantity += inbound.getQuantity();
        }

        document.add(table);

        // 添加统计信息
        Paragraph summary = new Paragraph("\n统计信息：", PdfUtil.createSubTitleFont());
        document.add(summary);

        Paragraph totalRecords = new Paragraph("入库单数：" + inbounds.size(), PdfUtil.createTableFont());
        totalRecords.setSpacingBefore(10);
        document.add(totalRecords);

        Paragraph totalQty = new Paragraph("入库总量：" + totalQuantity, PdfUtil.createTableFont());
        document.add(totalQty);

        document.close();
        return outputStream.toByteArray();
    }

    /**
     * 生成出库统计报表PDF
     */
    public byte[] generateOutboundReport(LocalDateTime startDate, LocalDateTime endDate) throws DocumentException, IOException {
        return PdfUtil.generatePdf(document -> {
            // 添加标题
            PdfUtil.addTitle(document, "出库统计报表");

            String dateRange = "";
            if (startDate != null && endDate != null) {
                dateRange = startDate.format(DATE_FORMATTER) + " 至 " + endDate.format(DATE_FORMATTER);
            } else {
                dateRange = "全部";
            }
            PdfUtil.addSubTitle(document, "统计期间：" + dateRange);
            PdfUtil.addSubTitle(document, "生成时间：" + LocalDateTime.now().format(DATE_TIME_FORMATTER));

            // 获取出库记录
            List<Outbound> outbounds;
            if (startDate != null && endDate != null) {
                outbounds = outboundRepository.findByOutboundDateBetween(startDate, endDate);
            } else {
                outbounds = outboundRepository.findAll();
            }

            // 创建表格
            PdfPTable table = PdfUtil.createTable(6);
            try {
                table.setWidths(new float[]{2, 2, 3, 1.5f, 2, 3});
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }

            // 添加表头
            PdfUtil.addHeaderCell(table, "出库单号");
            PdfUtil.addHeaderCell(table, "商品编号");
            PdfUtil.addHeaderCell(table, "商品名称");
            PdfUtil.addHeaderCell(table, "数量");
            PdfUtil.addHeaderCell(table, "出库日期");
            PdfUtil.addHeaderCell(table, "客户");

            // 添加数据
            int totalQuantity = 0;

            for (Outbound outbound : outbounds) {
                PdfUtil.addCell(table, outbound.getOutboundNo() != null ? outbound.getOutboundNo() : "");
                PdfUtil.addCell(table, outbound.getProduct() != null ? outbound.getProduct().getCode() : "");
                PdfUtil.addLeftAlignCell(table, outbound.getProduct() != null ? outbound.getProduct().getName() : "");
                PdfUtil.addCell(table, String.valueOf(outbound.getQuantity()));
                PdfUtil.addCell(table, outbound.getOutboundDate() != null ? outbound.getOutboundDate().format(DATE_FORMATTER) : "");
                PdfUtil.addCell(table, outbound.getCustomer() != null ? outbound.getCustomer().getName() : "");

                totalQuantity += outbound.getQuantity();
            }

            document.add(table);

            // 添加统计信息
            Paragraph summary = new Paragraph("\n统计信息：", PdfUtil.createSubTitleFont());
            document.add(summary);

            Paragraph totalRecords = new Paragraph("出库单数：" + outbounds.size(), PdfUtil.createTableFont());
            totalRecords.setSpacingBefore(10);
            document.add(totalRecords);

            Paragraph totalQty = new Paragraph("出库总量：" + totalQuantity, PdfUtil.createTableFont());
            document.add(totalQty);
        });
    }

    /**
     * 生成低库存报表PDF
     */
    public byte[] generateLowStockReport() throws DocumentException, IOException {
        return PdfUtil.generatePdf(document -> {
            // 添加标题
            PdfUtil.addTitle(document, "低库存预警报表");
            PdfUtil.addSubTitle(document, "生成时间：" + LocalDateTime.now().format(DATE_TIME_FORMATTER));

            // 获取低库存商品
            List<Product> products = productRepository.findActiveLowStockProducts();

            // 创建表格
            PdfPTable table = PdfUtil.createTable(7);
            try {
                table.setWidths(new float[]{1, 2, 3, 2, 1.5f, 1.5f, 1.5f});
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }

            // 添加表头
            PdfUtil.addHeaderCell(table, "ID");
            PdfUtil.addHeaderCell(table, "商品编号");
            PdfUtil.addHeaderCell(table, "商品名称");
            PdfUtil.addHeaderCell(table, "分类");
            PdfUtil.addHeaderCell(table, "当前库存");
            PdfUtil.addHeaderCell(table, "最低库存");
            PdfUtil.addHeaderCell(table, "缺口");

            // 添加数据
            int totalGap = 0;

            for (Product product : products) {
                int stockQty = product.getStockQty() != null ? product.getStockQty() : 0;
                int minStock = product.getMinStock() != null ? product.getMinStock() : 0;
                int gap = minStock - stockQty;

                PdfUtil.addCell(table, String.valueOf(product.getId()));
                PdfUtil.addCell(table, product.getCode() != null ? product.getCode() : "");
                PdfUtil.addLeftAlignCell(table, product.getName() != null ? product.getName() : "");
                PdfUtil.addCell(table, product.getCategory() != null ? product.getCategory().getName() : "");
                PdfUtil.addCell(table, String.valueOf(stockQty));
                PdfUtil.addCell(table, String.valueOf(minStock));
                PdfUtil.addCell(table, String.valueOf(gap));

                totalGap += gap;
            }

            document.add(table);

            // 添加统计信息
            Paragraph summary = new Paragraph("\n统计信息：", PdfUtil.createSubTitleFont());
            document.add(summary);

            Paragraph lowStockCount = new Paragraph("低库存商品数：" + products.size(), PdfUtil.createTableFont());
            lowStockCount.setSpacingBefore(10);
            document.add(lowStockCount);

            Paragraph totalGapPara = new Paragraph("总缺口量：" + totalGap, PdfUtil.createTableFont());
            document.add(totalGapPara);
        });
    }
}
