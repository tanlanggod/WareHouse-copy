package com.warehouse.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 仓库实体
 */
@Entity
@Table(name = "warehouse")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler"})
@EqualsAndHashCode(callSuper = true)
public class Warehouse extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "仓库名称不能为空")
    @Size(max = 100, message = "仓库名称长度不能超过100个字符")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "仓库地址不能为空")
    @Size(max = 255, message = "仓库地址长度不能超过255个字符")
    @Column(nullable = false, length = 255)
    private String address;

    @Size(max = 50, message = "仓库编号长度不能超过50个字符")
    @Column(length = 50, unique = true)
    private String code;

    @Size(max = 50, message = "联系人姓名长度不能超过50个字符")
    @Column(length = 50)
    private String contactPerson;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用'")
    private Integer status = 1;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<WarehouseLocation> locations;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
    }

    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
    }

    /**
     * 获取状态描述
     */
    @JsonIgnore
    public String getStatusDescription() {
        return status != null && status == 1 ? "启用" : "禁用";
    }

    /**
     * 检查是否启用
     */
    @JsonIgnore
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 自定义toString方法，避免循环引用
     */
    @Override
    public String toString() {
        return "Warehouse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", code='" + code + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}