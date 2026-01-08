package com.warehouse.repository;

import com.warehouse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * 用户仓储接口，提供账号查询与存在性校验。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // 新增的多认证方式相关方法
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    User findByWechatOpenId(String wechatOpenId);
    Optional<User> findByEmail(String email);
}

