package com.warehouse.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 基础版本控制实体类
 * 继承自BaseAuditEntity，增加乐观锁版本控制
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseVersionEntity extends BaseAuditEntity {

    @Version
    @Column(name = "version")
    private Long version = 0L;
}