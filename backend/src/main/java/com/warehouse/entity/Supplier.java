package com.warehouse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 供应商实体，记录供货方基础信息。
 */
@Entity
@Table(name = "supplier")
@Data
@EqualsAndHashCode(callSuper = true)
public class Supplier extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;
}

