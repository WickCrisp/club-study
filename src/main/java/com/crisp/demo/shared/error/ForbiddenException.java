package com.crisp.demo.shared.error;

/**
 * 权限不足异常 — 由全局异常处理器转换为 HTTP 403
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
