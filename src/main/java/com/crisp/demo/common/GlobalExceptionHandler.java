package com.crisp.demo.common;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理 — 消除 Controller 层重复 try-catch
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 → 200 + 错误消息 */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getMessage());
    }

    /** 参数校验失败 → 400 + 字段级错误信息 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.error(400, msg);      // 400 Bad Request，返回字段名+错误描述
    }

    /** 其他未捕获异常 → 500 + 通用提示 */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error("系统错误");
    }
}
