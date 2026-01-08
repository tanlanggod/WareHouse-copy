package com.warehouse.config;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 自定义缓存Key生成器
 * 用于商品列表查询的缓存Key生成
 *
 * @author Warehouse System
 */
@Component
public class CacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder sb = new StringBuilder();

        // 添加类名和方法名
        sb.append(target.getClass().getSimpleName()).append(":");
        sb.append(method.getName()).append(":");

        // 添加参数值
        if (params != null && params.length > 0) {
            Arrays.stream(params).forEach(param -> {
                if (param != null) {
                    sb.append(param).append(":");
                } else {
                    sb.append("null:");
                }
            });
        }

        // 移除最后的冒号
        if (sb.charAt(sb.length() - 1) == ':') {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }
}