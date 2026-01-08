package com.warehouse.service;

import com.warehouse.common.Result;
import com.warehouse.entity.SystemConfig;
import com.warehouse.repository.BaseRepository;
import com.warehouse.repository.SystemConfigRepository;
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
 * 系统配置服务
 */
@Service
@Transactional
public class SystemConfigService extends BaseService<SystemConfig, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(SystemConfigService.class);

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @Override
    protected BaseRepository<SystemConfig, Integer> getRepository() {
        return systemConfigRepository;
    }

    protected Integer getCurrentUserId() {
        try {
            return UserContext.getCurrentUserId();
        } catch (Exception e) {
            return 1; // 系统用户ID
        }
    }

    /**
     * 获取所有系统配置
     */
    @Cacheable(value = "systemConfigs", key = "'all'")
    public Result<List<SystemConfig>> getAllConfigs() {
        try {
            List<SystemConfig> configs = findAll();
            return Result.success("获取系统配置列表成功", configs);
        } catch (Exception e) {
            logger.error("获取系统配置列表失败: {}", e.getMessage(), e);
            return Result.error("获取系统配置列表失败：" + e.getMessage());
        }
    }

    /**
     * 分页获取系统配置
     */
    public Result<Page<SystemConfig>> getConfigs(Pageable pageable) {
        try {
            Page<SystemConfig> configs = findAll(pageable);
            return Result.success("获取系统配置列表成功", configs);
        } catch (Exception e) {
            logger.error("获取系统配置列表失败: {}", e.getMessage(), e);
            return Result.error("获取系统配置列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取系统配置
     */
    @Cacheable(value = "systemConfig", key = "#id")
    public Result<SystemConfig> getConfigById(Integer id) {
        try {
            Optional<SystemConfig> config = findActiveById(id);
            if (config.isPresent()) {
                return Result.success("获取系统配置信息成功", config.get());
            } else {
                return Result.error("系统配置不存在");
            }
        } catch (Exception e) {
            logger.error("获取系统配置信息失败: {}", e.getMessage(), e);
            return Result.error("获取系统配置信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据配置键获取配置
     */
    @Cacheable(value = "systemConfigByKey", key = "#configKey")
    public Result<SystemConfig> getConfigByKey(String configKey) {
        try {
            if (!StringUtils.hasText(configKey)) {
                return Result.error("配置键不能为空");
            }

            Optional<SystemConfig> config = systemConfigRepository.findActiveByKey(configKey);
            if (config.isPresent()) {
                return Result.success("获取系统配置信息成功", config.get());
            } else {
                return Result.error("系统配置不存在");
            }
        } catch (Exception e) {
            logger.error("获取系统配置信息失败: {}", e.getMessage(), e);
            return Result.error("获取系统配置信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据配置键获取配置值
     */
    @Cacheable(value = "systemConfigValue", key = "#configKey")
    public Result<String> getConfigValue(String configKey) {
        try {
            if (!StringUtils.hasText(configKey)) {
                return Result.error("配置键不能为空");
            }

            Optional<SystemConfig> config = systemConfigRepository.findActiveByKey(configKey);
            if (config.isPresent()) {
                return Result.success("获取配置值成功", config.get().getConfigValue());
            } else {
                return Result.success("配置不存在", null);
            }
        } catch (Exception e) {
            logger.error("获取配置值失败: {}", e.getMessage(), e);
            return Result.error("获取配置值失败：" + e.getMessage());
        }
    }

    /**
     * 创建系统配置
     */
    @CacheEvict(value = {"systemConfigs", "systemConfig", "systemConfigByKey", "systemConfigValue"}, allEntries = true)
    public Result<SystemConfig> createConfig(SystemConfig config) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            // 验证配置键不能为空
            if (!StringUtils.hasText(config.getConfigKey())) {
                return Result.error("配置键不能为空");
            }

            // 检查配置键是否已存在
            if (systemConfigRepository.findActiveByKey(config.getConfigKey()).isPresent()) {
                return Result.error("配置键已存在");
            }

            // 设置默认描述（如果没有提供）
            if (!StringUtils.hasText(config.getConfigDesc())) {
                config.setConfigDesc("系统配置项：" + config.getConfigKey());
            }

            SystemConfig savedConfig = create(config);
            logger.info("创建系统配置成功，ID: {}, 键: {}, 值: {}",
                savedConfig.getId(), savedConfig.getConfigKey(), savedConfig.getConfigValue());

            return Result.success("创建系统配置成功", savedConfig);
        } catch (Exception e) {
            logger.error("创建系统配置失败: {}", e.getMessage(), e);
            return Result.error("创建系统配置失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 更新系统配置
     */
    @CacheEvict(value = {"systemConfigs", "systemConfig", "systemConfigByKey", "systemConfigValue"}, allEntries = true)
    public Result<SystemConfig> updateConfig(Integer id, SystemConfig config) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            Optional<SystemConfig> existingConfig = findActiveById(id);
            if (!existingConfig.isPresent()) {
                return Result.error("系统配置不存在");
            }

            SystemConfig configToUpdate = existingConfig.get();

            // 如果配置键发生变化，检查唯一性
            if (!config.getConfigKey().equals(configToUpdate.getConfigKey()) &&
                systemConfigRepository.findActiveByKey(config.getConfigKey()).isPresent()) {
                return Result.error("配置键已存在");
            }

            // 验证配置键不能为空
            if (!StringUtils.hasText(config.getConfigKey())) {
                return Result.error("配置键不能为空");
            }

            // 更新字段
            configToUpdate.setConfigKey(config.getConfigKey());
            configToUpdate.setConfigValue(config.getConfigValue());
            if (StringUtils.hasText(config.getConfigDesc())) {
                configToUpdate.setConfigDesc(config.getConfigDesc());
            }

            SystemConfig savedConfig = update(configToUpdate);
            logger.info("更新系统配置成功，ID: {}, 键: {}, 值: {}",
                savedConfig.getId(), savedConfig.getConfigKey(), savedConfig.getConfigValue());

            return Result.success("更新系统配置成功", savedConfig);
        } catch (Exception e) {
            logger.error("更新系统配置失败: {}", e.getMessage(), e);
            return Result.error("更新系统配置失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 删除系统配置
     */
    @CacheEvict(value = {"systemConfigs", "systemConfig", "systemConfigByKey", "systemConfigValue"}, allEntries = true)
    public Result<String> deleteConfig(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            Optional<SystemConfig> config = findActiveById(id);
            if (!config.isPresent()) {
                return Result.error("系统配置不存在");
            }

            // TODO: 检查配置是否被系统关键功能使用
            // 这里可以添加业务逻辑，检查配置是否被其他模块使用

            softDelete(id);
            logger.info("删除系统配置成功，ID: {}, 键: {}", id, config.get().getConfigKey());

            return Result.success("删除系统配置成功", "删除成功");
        } catch (Exception e) {
            logger.error("删除系统配置失败: {}", e.getMessage(), e);
            return Result.error("删除系统配置失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 恢复删除的系统配置
     */
    @CacheEvict(value = {"systemConfigs", "systemConfig", "systemConfigByKey", "systemConfigValue"}, allEntries = true)
    public Result<String> restoreConfig(Integer id) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            restore(id);
            logger.info("恢复系统配置成功，ID: {}", id);

            return Result.success("恢复系统配置成功", "恢复成功");
        } catch (Exception e) {
            logger.error("恢复系统配置失败: {}", e.getMessage(), e);
            return Result.error("恢复系统配置失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 获取已删除的系统配置
     */
    public Result<List<SystemConfig>> getDeletedConfigs() {
        try {
            List<SystemConfig> deletedConfigs = findAllDeleted();
            return Result.success("获取已删除系统配置列表成功", deletedConfigs);
        } catch (Exception e) {
            logger.error("获取已删除系统配置列表失败: {}", e.getMessage(), e);
            return Result.error("获取已删除系统配置列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据配置键搜索配置
     */
    public Result<List<SystemConfig>> searchConfigs(String keyword) {
        try {
            List<SystemConfig> configs = systemConfigRepository.findActiveByKeyContaining(keyword);
            return Result.success("搜索系统配置成功", configs);
        } catch (Exception e) {
            logger.error("搜索系统配置失败: {}", e.getMessage(), e);
            return Result.error("搜索系统配置失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除系统配置
     */
    @Transactional
    public Result<String> batchDeleteConfigs(List<Integer> ids) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            // TODO: 检查配置是否被系统关键功能使用
            softDeleteAll(ids);
            logger.info("批量删除系统配置成功，ID数量: {}", ids.size());

            return Result.success("批量删除系统配置成功", "删除成功");
        } catch (Exception e) {
            logger.error("批量删除系统配置失败: {}", e.getMessage(), e);
            return Result.error("批量删除系统配置失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 批量恢复系统配置
     */
    @Transactional
    public Result<String> batchRestoreConfigs(List<Integer> ids) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            restoreAll(ids);
            logger.info("批量恢复系统配置成功，ID数量: {}", ids.size());

            return Result.success("批量恢复系统配置成功", "恢复成功");
        } catch (Exception e) {
            logger.error("批量恢复系统配置失败: {}", e.getMessage(), e);
            return Result.error("批量恢复系统配置失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 刷新配置缓存
     */
    @CacheEvict(value = {"systemConfigs", "systemConfig", "systemConfigByKey", "systemConfigValue"}, allEntries = true)
    public Result<String> refreshConfigCache() {
        try {
            // 这个方法主要用于手动刷新缓存
            return Result.success("配置缓存刷新成功", "刷新成功");
        } catch (Exception e) {
            logger.error("刷新配置缓存失败: {}", e.getMessage(), e);
            return Result.error("刷新配置缓存失败：" + e.getMessage());
        }
    }

    /**
     * 根据模块获取配置列表
     */
    public Result<List<SystemConfig>> getConfigsByModule(String module) {
        try {
            // 可以根据configKey的前缀或特定格式来区分模块
            List<SystemConfig> allConfigs = findAll();
            List<SystemConfig> moduleConfigs = allConfigs.stream()
                .filter(config -> config.getConfigKey().startsWith(module + "_"))
                .collect(java.util.stream.Collectors.toList());

            return Result.success("获取模块配置成功", moduleConfigs);
        } catch (Exception e) {
            logger.error("获取模块配置失败: {}", e.getMessage(), e);
            return Result.error("获取模块配置失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新配置
     */
    @Transactional
    @CacheEvict(value = {"systemConfigs", "systemConfig", "systemConfigByKey", "systemConfigValue"}, allEntries = true)
    public Result<List<SystemConfig>> batchUpdateConfigs(List<SystemConfig> configs) {
        UserContext.setCurrentUserId(getCurrentUserId());

        try {
            List<SystemConfig> savedConfigs = new java.util.ArrayList<>();

            for (SystemConfig config : configs) {
                if (config.getId() != null) {
                    Optional<SystemConfig> existingConfig = findActiveById(config.getId());
                    if (existingConfig.isPresent()) {
                        SystemConfig configToUpdate = existingConfig.get();
                        configToUpdate.setConfigValue(config.getConfigValue());
                        if (StringUtils.hasText(config.getConfigDesc())) {
                            configToUpdate.setConfigDesc(config.getConfigDesc());
                        }
                        savedConfigs.add(update(configToUpdate));
                    }
                }
            }

            logger.info("批量更新系统配置成功，数量: {}", savedConfigs.size());
            return Result.success("批量更新系统配置成功", savedConfigs);
        } catch (Exception e) {
            logger.error("批量更新系统配置失败: {}", e.getMessage(), e);
            return Result.error("批量更新系统配置失败：" + e.getMessage());
        } finally {
            UserContext.clear();
        }
    }
}