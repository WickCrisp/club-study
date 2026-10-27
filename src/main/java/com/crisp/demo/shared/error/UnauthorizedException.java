package com.crisp.demo.shared.error;

/**
 * 未认证 — 没有令牌、令牌非法或已过期
 *
 * 由全局异常处理器映射为 HTTP 401，与令牌缺失时拦截器直接写出的响应保持一致。
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
