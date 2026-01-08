package com.warehouse.controller;

import com.warehouse.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 报表控制器，提供各类PDF报表生成接口
 */
@RestController
@RequestMapping("/reports")
@CrossOrigin
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 生成库存报表PDF
     */
    @GetMapping("/stock/pdf")
    public ResponseEntity<byte[]> generateStockReportPdf() {
        try {
            byte[] data = reportService.generateStockReport();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "stock_report.pdf");

            return ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 生成入库统计报表PDF
     */
    @GetMapping("/inbound/pdf")
    public ResponseEntity<byte[]> generateInboundReportPdf(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        try {
            byte[] data = reportService.generateInboundReport(startDate, endDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "inbound_report.pdf");

            return ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 生成出库统计报表PDF
     */
    @GetMapping("/outbound/pdf")
    public ResponseEntity<byte[]> generateOutboundReportPdf(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        try {
            byte[] data = reportService.generateOutboundReport(startDate, endDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "outbound_report.pdf");

            return ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 生成低库存报表PDF
     */
    @GetMapping("/low-stock/pdf")
    public ResponseEntity<byte[]> generateLowStockReportPdf() {
        try {
            byte[] data = reportService.generateLowStockReport();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "low_stock_report.pdf");

            return ResponseEntity.ok().headers(headers).body(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
