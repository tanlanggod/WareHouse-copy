package com.warehouse.repository;

import com.warehouse.entity.Outbound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库记录仓储接口，支持多条件分页查询。
 */
@Repository
public interface OutboundRepository extends BaseRepository<Outbound, Integer> {

    @Query("SELECT o FROM Outbound o WHERE o.isDeleted = 0 AND " +
           "(:startDate IS NULL OR o.outboundDate >= :startDate) AND " +
           "(:endDate IS NULL OR o.outboundDate <= :endDate) AND " +
           "(:productId IS NULL OR o.product.id = :productId) AND " +
           "(:customerId IS NULL OR o.customer.id = :customerId)")
    Page<Outbound> findActiveByConditions(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("productId") Integer productId,
                                          @Param("customerId") Integer customerId,
                                          Pageable pageable);

    List<Outbound> findByOutboundDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT o FROM Outbound o WHERE o.isDeleted = 0 AND o.outboundDate BETWEEN :startDate AND :endDate")
    List<Outbound> findActiveByOutboundDateBetween(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    // 根据商品ID查找出库记录
    List<Outbound> findByProductId(Integer productId);

    @Query("SELECT o FROM Outbound o WHERE o.isDeleted = 0 AND o.product.id = :productId")
    List<Outbound> findActiveByProductId(@Param("productId") Integer productId);

    // 根据客户ID查找出库记录
    List<Outbound> findByCustomerId(Integer customerId);

    @Query("SELECT o FROM Outbound o WHERE o.isDeleted = 0 AND o.customer.id = :customerId")
    List<Outbound> findActiveByCustomerId(@Param("customerId") Integer customerId);

    // 根据操作人查找出库记录
    List<Outbound> findByOperatorId(Integer operatorId);

    @Query("SELECT o FROM Outbound o WHERE o.isDeleted = 0 AND o.operator.id = :operatorId")
    List<Outbound> findActiveByOperatorId(@Param("operatorId") Integer operatorId);
}

