package com.crisp.demo.infrastructure.web;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 认证/授权失败时的统一响应输出
 *
 * 约定：HTTP 状态码与响应体 code 必须一致（401 未认证、403 无权限），
 * 前端拦截器据此判断"该跳登录"还是"只提示无权限"。
 * 消息均为常量，不含用户输入，因此可以直接拼接 JSON。
 */
final class AuthResponses {

    private AuthResponses() {
    }

    static boolean write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\",\"data\":null}");
        return false;
    }

    static boolean unauthorized(HttpServletResponse response, String message) throws IOException {
        return write(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    static boolean forbidden(HttpServletResponse response, String message) throws IOException {
        return write(response, HttpServletResponse.SC_FORBIDDEN, message);
    }
}
