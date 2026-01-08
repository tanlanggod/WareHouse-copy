package com.warehouse.config;

import com.warehouse.interceptor.OperationLogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc 统一配置类
 * 包含：拦截器注册、类型转换器配置等
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private StringToLocalDateTimeConverter stringToLocalDateTimeConverter;

    @Autowired
    private OperationLogInterceptor operationLogInterceptor;

    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        // 注册字符串到LocalDateTime的类型转换器
        registry.addConverter(stringToLocalDateTimeConverter);
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // 注册操作日志拦截器
        registry.addInterceptor(operationLogInterceptor)
                .addPathPatterns("/api/**") // 拦截所有API请求
                .excludePathPatterns(
                        "/api/actuator/**",    // 排除监控端点
                        "/api/health",         // 排除健康检查
                        "/api/auth/login",     // 排除登录接口
                        "/api/auth/register",  // 排除注册接口
                        "/api/error"           // 排除错误处理
                );
    }
}

