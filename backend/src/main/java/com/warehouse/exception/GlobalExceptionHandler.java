package com.warehouse.exception;

import com.warehouse.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器，统一处理所有Controller抛出的异常，返回统一的错误响应格式。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常（RuntimeException）
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Object> handleRuntimeException(RuntimeException e) {
        logger.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 处理参数验证异常（@RequestBody参数验证）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        logger.warn("参数验证失败: {}", errorMessage);
        return Result.error(400, "参数验证失败: " + errorMessage);
    }

    /**
     * 处理表单绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        logger.warn("表单绑定失败: {}", errorMessage);
        return Result.error(400, "表单绑定失败: " + errorMessage);
    }

    /**
     * 处理约束违反异常（@RequestParam参数验证）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        logger.warn("约束验证失败: {}", errorMessage);
        return Result.error(400, "约束验证失败: " + errorMessage);
    }

    /**
     * 处理HTTP消息不可读异常（JSON解析错误等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.error("请求体解析失败", e);
        String errorMsg = e.getMessage();
        String message = "请求体格式错误，请检查JSON格式";
        if (errorMsg != null && errorMsg.contains("JSON")) {
            message = "JSON格式错误: " + errorMsg;
        }
        return Result.error(400, message);
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleException(Exception e) {
        logger.error("系统异常", e);
        return Result.error(500, "系统内部错误，请联系管理员");
    }
}

