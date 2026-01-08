package com.warehouse.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 商品实体，存储商品基本属性及库存信息。
 */
@Entity
@Table(name = "product")
@Data
public class Product extends BaseVersionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "stock_qty")
    private Integer stockQty = 0;

    @Column(name = "min_stock")
    private Integer minStock = 0;

    @Column(length = 20)
    private String unit = "件";

    @Column(length = 100)
    private String barcode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;
}

