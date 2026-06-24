package com.crisp.demo.common;

/**
 * 业务异常 — 由全局异常处理器捕获，返回 Result.error(message)
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
