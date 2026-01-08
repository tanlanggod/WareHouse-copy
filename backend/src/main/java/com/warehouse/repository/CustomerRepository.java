package com.warehouse.repository;

import com.warehouse.entity.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 客户仓储接口。
 */
@Repository
public interface CustomerRepository extends BaseRepository<Customer, Integer> {

    // 根据名称查找客户
    Optional<Customer> findByName(String name);

    @Query("SELECT c FROM Customer c WHERE c.name = :name AND c.isDeleted = 0")
    Optional<Customer> findActiveByName(@Param("name") String name);

    // 根据状态查找客户
    List<Customer> findByStatus(Integer status);

    @Query("SELECT c FROM Customer c WHERE c.status = :status AND c.isDeleted = 0")
    List<Customer> findActiveByStatus(@Param("status") Integer status);

    // 模糊搜索客户
    List<Customer> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c FROM Customer c WHERE c.isDeleted = 0 AND " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Customer> findActiveByNameContaining(@Param("keyword") String keyword);
}

