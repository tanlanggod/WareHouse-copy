package com.warehouse.service;

import com.warehouse.entity.BaseAuditEntity;
import com.warehouse.repository.BaseRepository;
import com.warehouse.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 基础服务类
 * 提供通用的CRUD操作，包含审计功能
 */
public abstract class BaseService<T extends BaseAuditEntity, ID> {

    @Autowired
    protected AuditService auditService;

    /**
     * 获取对应的Repository
     */
    protected abstract BaseRepository<T, ID> getRepository();

    /**
     * 创建实体
     */
    @Transactional
    public T create(T entity) {
        auditService.setCreateInfo(entity);
        return getRepository().save(entity);
    }

    /**
     * 更新实体
     */
    @Transactional
    public T update(T entity) {
        if (!auditService.canOperate(entity)) {
            throw new IllegalArgumentException("无权操作该实体");
        }
        auditService.setUpdateInfo(entity);
        return getRepository().save(entity);
    }

    /**
     * 根据ID查找实体
     */
    public Optional<T> findById(ID id) {
        return getRepository().findActiveById(id);
    }

    /**
     * 根据ID查找未删除的实体（别名方法）
     */
    public Optional<T> findActiveById(ID id) {
        return getRepository().findActiveById(id);
    }

    /**
     * 查找所有未删除的实体
     */
    public List<T> findAll() {
        return getRepository().findAllActive();
    }

    /**
     * 分页查找未删除的实体
     */
    public Page<T> findAll(Pageable pageable) {
        return getRepository().findAllActive(pageable);
    }

    /**
     * 逻辑删除实体
     */
    @Transactional
    public void softDelete(ID id) {
        Optional<T> entityOpt = getRepository().findActiveById(id);
        if (entityOpt.isPresent()) {
            T entity = entityOpt.get();
            if (!auditService.canDelete(entity)) {
                throw new IllegalArgumentException("无权删除该实体");
            }

            Integer currentUserId = getCurrentUserId();
            getRepository().softDelete(id, currentUserId, LocalDateTime.now());
        }
    }

    /**
     * 批量逻辑删除实体
     */
    @Transactional
    public void softDeleteAll(Iterable<ID> ids) {
        Integer currentUserId = getCurrentUserId();
        getRepository().softDeleteAll(ids, currentUserId, LocalDateTime.now());
    }

    /**
     * 恢复删除的实体
     */
    @Transactional
    public void restore(ID id) {
        getRepository().restore(id);
    }

    /**
     * 批量恢复删除的实体
     */
    @Transactional
    public void restoreAll(Iterable<ID> ids) {
        getRepository().restoreAll(ids);
    }

    /**
     * 查找已删除的实体
     */
    public List<T> findAllDeleted() {
        return getRepository().findAllDeleted();
    }

    /**
     * 分页查找已删除的实体
     */
    public Page<T> findAllDeleted(Pageable pageable) {
        return getRepository().findAllDeleted(pageable);
    }

    /**
     * 根据创建人查找实体
     */
    public List<T> findByCreatorId(Integer creatorId) {
        return getRepository().findByCreatorId(creatorId);
    }

    /**
     * 根据修改人查找实体
     */
    public List<T> findByUpdaterId(Integer updaterId) {
        return getRepository().findByUpdaterId(updaterId);
    }

    /**
     * 检查实体是否存在且未删除
     */
    public boolean existsById(ID id) {
        return getRepository().existsActiveById(id);
    }

    /**
     * 检查实体是否存在且未删除（别名方法）
     */
    public boolean existsActiveById(ID id) {
        return getRepository().existsActiveById(id);
    }

    /**
     * 统计所有未删除的实体数量
     */
    public long count() {
        return getRepository().count();
    }

    /**
     * 永久删除实体（物理删除）
     */
    @Transactional
    public void permanentlyDelete(ID id) {
        // 永久删除需要特殊权限
        // 这里应该添加权限检查逻辑
        getRepository().deleteById(id);
    }

    /**
     * 获取当前用户ID
     */
    protected Integer getCurrentUserId() {
        try {
            return UserContext.getCurrentUserId();
        } catch (Exception e) {
            return 1; // 系统用户ID
        }
    }
}