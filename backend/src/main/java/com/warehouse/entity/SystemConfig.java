package com.warehouse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 系统配置实体，存放可动态调整的键值对参数。
 */
@Entity
@Table(name = "system_config")
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemConfig extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_value", length = 255)
    private String configValue;

    @Column(name = "config_desc", length = 255)
    private String configDesc;
}

