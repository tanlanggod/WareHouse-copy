package com.warehouse.common;

import lombok.Data;
import java.util.List;

/**
 * 分页结果包装对象，记录总条数与当前页数据。
 */
@Data
public class PageResult<T> {
    private Long total;
    private List<T> records;
    private Integer currentPage;      // 当前页码（从1开始）
    private Integer pageSize;         // 每页大小
    private Integer totalPages;       // 总页数

    public PageResult() {
    }

    public PageResult(Long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    public PageResult(List<T> records, Long total, Integer currentPage,
                     Integer pageSize, Integer totalPages) {
        this.records = records;
        this.total = total;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
    }

    public PageResult(List<T> records, Long total, Integer currentPage, Integer pageSize) {
        this.records = records;
        this.total = total;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }
}

