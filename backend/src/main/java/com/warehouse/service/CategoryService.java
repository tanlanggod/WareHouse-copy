package com.warehouse.service;

import com.warehouse.common.Result;
import com.warehouse.entity.Category;
import com.warehouse.repository.BaseRepository;
import com.warehouse.repository.CategoryRepository;
import com.warehouse.util.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 类别服务
 */
@Service
@Transactional
public class CategoryService extends BaseService<Category, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    protected BaseRepository<Category, Integer> getRepository() {
        return categoryRepository;
    }

    protected Integer getCurrentUserId() {
        try {
            return UserContext.getCurrentUserId();
        } catch (Exception e) {
            return 1; // 系统用户ID
        }
    }

    /**
     * 获取所有类别
     */
    @Cacheable(value = "categories", key = "'all'")
    public Result<List<Category>> getAllCategories() {
        try {
            List<Category> categories = findAll();
            return Result.success("获取类别列表成功", categories);
        } catch (Exception e) {
            logger.error("获取类别列表失败: {}", e.getMessage(), e);
            return Result.error("获取类别列表失败：" + e.getMessage());
        }
    }

    /**
     * 分页获取类别
     */
    public Result<Page<Category>> getCategories(Pageable pageable) {
        try {
            Page<Category> categories = findAll(pageable);
            return Result.success("获取类别列表成功", categories);
        } catch (Exception e) {
            logger.error("获取类别列表失败: {}", e.getMessage(), e);
            return Result.error("获取类别列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取类别
     */
    @Cacheable(value = "category", key = "#id")
    public Result<Category> getCategoryById(Integer id) {
        try {
            Optional<Category> category = findActiveById(id);
            if (category.isPresent()) {
                return Result.success("获取类别信息成功", category.get());
            } else {
                return Result.error("类别不存在");
            }
        } catch (Exception e) {
            logger.error("获取类别信息失败: {}", e.getMessage(), e);
            return Result.error("获取类别信息失败：" + e.getMessage());
        }
    }

    /**
     * 创建类别
     */
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public Result<Category> createCategory(Category category) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            // 验证类别名称唯一性
            if (categoryRepository.existsActiveByName(category.getName())) {
                return Result.error("类别名称已存在");
            }

            // 验证必填字段
            if (!StringUtils.hasText(category.getName())) {
                return Result.error("类别名称不能为空");
            }

            Category savedCategory = create(category);
            logger.info("创建类别成功，ID: {}, 名称: {}", savedCategory.getId(), savedCategory.getName());

            return Result.success("创建类别成功", savedCategory);
        } catch (Exception e) {
            logger.error("创建类别失败: {}", e.getMessage(), e);
            return Result.error("创建类别失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 更新类别
     */
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public Result<Category> updateCategory(Integer id, Category category) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            Optional<Category> existingCategory = findActiveById(id);
            if (!existingCategory.isPresent()) {
                return Result.error("类别不存在");
            }

            Category categoryToUpdate = existingCategory.get();

            // 验证类别名称唯一性（排除当前类别）
            if (!category.getName().equals(categoryToUpdate.getName()) &&
                categoryRepository.existsActiveByName(category.getName())) {
                return Result.error("类别名称已存在");
            }

            // 验证必填字段
            if (!StringUtils.hasText(category.getName())) {
                return Result.error("类别名称不能为空");
            }

            // 更新字段
            categoryToUpdate.setName(category.getName());
            categoryToUpdate.setDescription(category.getDescription());

            Category savedCategory = update(categoryToUpdate);
            logger.info("更新类别成功，ID: {}, 名称: {}", savedCategory.getId(), savedCategory.getName());

            return Result.success("更新类别成功", savedCategory);
        } catch (Exception e) {
            logger.error("更新类别失败: {}", e.getMessage(), e);
            return Result.error("更新类别失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 删除类别
     */
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public Result<String> deleteCategory(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            Optional<Category> category = findActiveById(id);
            if (!category.isPresent()) {
                return Result.error("类别不存在");
            }

            // TODO: 检查是否有关联的产品
            // 这里可以添加业务逻辑，检查类别是否有关联的产品

            softDelete(id);
            logger.info("删除类别成功，ID: {}", id);

            return Result.success("删除类别成功", "删除成功");
        } catch (Exception e) {
            logger.error("删除类别失败: {}", e.getMessage(), e);
            return Result.error("删除类别失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 恢复删除的类别
     */
    @CacheEvict(value = {"categories", "category"}, allEntries = true)
    public Result<String> restoreCategory(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            restore(id);
            logger.info("恢复类别成功，ID: {}", id);

            return Result.success("恢复类别成功", "恢复成功");
        } catch (Exception e) {
            logger.error("恢复类别失败: {}", e.getMessage(), e);
            return Result.error("恢复类别失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 获取已删除的类别
     */
    public Result<List<Category>> getDeletedCategories() {
        try {
            List<Category> deletedCategories = findAllDeleted();
            return Result.success("获取已删除类别列表成功", deletedCategories);
        } catch (Exception e) {
            logger.error("获取已删除类别列表失败: {}", e.getMessage(), e);
            return Result.error("获取已删除类别列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据名称搜索类别
     */
    public Result<List<Category>> searchCategories(String name) {
        try {
            List<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name);
            return Result.success("搜索类别成功", categories);
        } catch (Exception e) {
            logger.error("搜索类别失败: {}", e.getMessage(), e);
            return Result.error("搜索类别失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除类别
     */
    @Transactional
    public Result<String> batchDeleteCategories(List<Integer> ids) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            // TODO: 检查是否有关联的产品
            softDeleteAll(ids);
            logger.info("批量删除类别成功，ID数量: {}", ids.size());

            return Result.success("批量删除类别成功", "删除成功");
        } catch (Exception e) {
            logger.error("批量删除类别失败: {}", e.getMessage(), e);
            return Result.error("批量删除类别失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 批量恢复类别
     */
    @Transactional
    public Result<String> batchRestoreCategories(List<Integer> ids) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            restoreAll(ids);
            logger.info("批量恢复类别成功，ID数量: {}", ids.size());

            return Result.success("批量恢复类别成功", "恢复成功");
        } catch (Exception e) {
            logger.error("批量恢复类别失败: {}", e.getMessage(), e);
            return Result.error("批量恢复类别失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }
}