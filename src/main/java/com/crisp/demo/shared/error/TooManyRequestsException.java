package com.crisp.demo.shared.error;

/**
 * 请求过于频繁 — 由登录限流等场景抛出，全局异常处理映射为 429
 */
public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(String message) {
        super(message);
    }
}
