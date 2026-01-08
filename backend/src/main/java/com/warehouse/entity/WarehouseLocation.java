package com.warehouse.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 仓库位置实体
 */
@Entity
@Table(name = "warehouse_location", indexes = {
    @Index(name = "idx_warehouse", columnList = "warehouse_id"),
    @Index(name = "idx_location", columnList = "rack_number, shelf_level, bin_number")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler"})
@EqualsAndHashCode(callSuper = true)
public class WarehouseLocation extends BaseVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false, foreignKey = @ForeignKey(name = "fk_location_warehouse"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "fieldHandler"})
    private Warehouse warehouse;

    
    @NotBlank(message = "货架号不能为空")
    @Size(max = 50, message = "货架号长度不能超过50个字符")
    @Column(name = "rack_number", nullable = false, length = 50)
    private String rackNumber;

    @NotBlank(message = "层号不能为空")
    @Size(max = 20, message = "层号长度不能超过20个字符")
    @Column(name = "shelf_level", nullable = false, length = 20)
    private String shelfLevel;

    @Size(max = 50, message = "货位号长度不能超过50个字符")
    @Column(name = "bin_number", length = 50)
    private String binNumber;

    @Size(max = 255, message = "位置描述长度不能超过255个字符")
    @Column(length = 255)
    private String description;

    @Column(columnDefinition = "TINYINT DEFAULT 1 COMMENT '状态：1-可用，0-禁用'")
    private Integer status = 1;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
    }

    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
    }

    /**
     * 获取完整位置信息
     */
    @JsonIgnore
    public String getFullLocation() {
        StringBuilder location = new StringBuilder();
        location.append(rackNumber != null ? rackNumber : "");

        if (shelfLevel != null && !shelfLevel.trim().isEmpty()) {
            location.append("-").append(shelfLevel);
        }

        if (binNumber != null && !binNumber.trim().isEmpty()) {
            location.append("-").append(binNumber);
        }

        return location.toString();
    }

    /**
     * 获取状态描述
     */
    @JsonIgnore
    public String getStatusDescription() {
        return status != null && status == 1 ? "可用" : "禁用";
    }

    /**
     * 检查是否可用
     */
    @JsonIgnore
    public boolean isAvailable() {
        return status != null && status == 1;
    }

    /**
     * 自定义toString方法，避免循环引用
     */
    @Override
    public String toString() {
        return "WarehouseLocation{" +
                "id=" + id +
                ", rackNumber='" + rackNumber + '\'' +
                ", shelfLevel='" + shelfLevel + '\'' +
                ", binNumber='" + binNumber + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", warehouseId=" + (warehouse != null ? warehouse.getId() : null) +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }

    // 添加用于测试的方法
    @Transient
    private Integer incomingWarehouseId;

    @JsonSetter("warehouseId")
    public void setWarehouseIdFromJson(int warehouseId) {
        this.incomingWarehouseId = warehouseId;
        // 如果warehouse对象为空，创建一个包含ID的临时对象
        if (this.warehouse == null) {
            this.warehouse = new Warehouse();
            this.warehouse.setId(warehouseId);
        }
    }

    @JsonIgnore
    public Integer getIncomingWarehouseId() {
        return this.incomingWarehouseId;
    }
}