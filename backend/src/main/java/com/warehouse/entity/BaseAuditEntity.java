package com.warehouse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 基础审计实体类
 * 包含创建时间、修改时间、创建人、修改人、逻辑删除等基础字段
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "creator_id")
    private Integer creatorId;

    @Column(name = "updater_id")
    private Integer updaterId;

    @Column(name = "is_deleted", columnDefinition = "TINYINT DEFAULT 0")
    private Integer isDeleted = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleter_id")
    private Integer deleterId;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 逻辑删除方法
     * @param deleterId 删除人ID
     */
    public void softDelete(Integer deleterId) {
        this.isDeleted = 1;
        this.deletedAt = LocalDateTime.now();
        this.deleterId = deleterId;
    }

    /**
     * 恢复删除状态
     */
    public void restore() {
        this.isDeleted = 0;
        this.deletedAt = null;
        this.deleterId = null;
    }

    /**
     * 检查是否已删除
     * @return true表示已删除，false表示未删除
     */
    @JsonIgnore
    public boolean isDeleted() {
        return this.isDeleted != null && this.isDeleted == 1;
    }
}