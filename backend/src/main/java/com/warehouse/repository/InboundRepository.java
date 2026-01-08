package com.warehouse.repository;

import com.warehouse.entity.Inbound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库记录仓储接口，支持多条件分页查询。
 */
@Repository
public interface InboundRepository extends BaseRepository<Inbound, Integer> {

    @Query("SELECT i FROM Inbound i WHERE i.isDeleted = 0 AND " +
           "(:startDate IS NULL OR i.inboundDate >= :startDate) AND " +
           "(:endDate IS NULL OR i.inboundDate <= :endDate) AND " +
           "(:productId IS NULL OR i.product.id = :productId) AND " +
           "(:supplierId IS NULL OR i.supplier.id = :supplierId)")
    Page<Inbound> findActiveByConditions(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         @Param("productId") Integer productId,
                                         @Param("supplierId") Integer supplierId,
                                         Pageable pageable);

    List<Inbound> findByInboundDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT i FROM Inbound i WHERE i.isDeleted = 0 AND i.inboundDate BETWEEN :startDate AND :endDate")
    List<Inbound> findActiveByInboundDateBetween(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    // 根据商品ID查找入库记录
    List<Inbound> findByProductId(Integer productId);

    @Query("SELECT i FROM Inbound i WHERE i.isDeleted = 0 AND i.product.id = :productId")
    List<Inbound> findActiveByProductId(@Param("productId") Integer productId);

    // 根据供应商ID查找入库记录
    List<Inbound> findBySupplierId(Integer supplierId);

    @Query("SELECT i FROM Inbound i WHERE i.isDeleted = 0 AND i.supplier.id = :supplierId")
    List<Inbound> findActiveBySupplierId(@Param("supplierId") Integer supplierId);

    // 根据操作人查找入库记录
    List<Inbound> findByOperatorId(Integer operatorId);

    @Query("SELECT i FROM Inbound i WHERE i.isDeleted = 0 AND i.operator.id = :operatorId")
    List<Inbound> findActiveByOperatorId(@Param("operatorId") Integer operatorId);
}

