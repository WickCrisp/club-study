package com.crisp.demo.shared.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 取客户端真实 IP
 *
 * 生产部署在 Nginx 后面时，request.getRemoteAddr() 拿到的是 127.0.0.1，
 * 真实地址在 X-Forwarded-For 的第一段。反向代理要配置：
 *   proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
 *
 * 注意：X-Forwarded-For 可以被伪造，只在可信代理后面才有意义。
 */
public final class ClientIp {

    private ClientIp() {
    }

    public static String resolve(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            String first = comma > 0 ? forwarded.substring(0, comma) : forwarded;
            if (!first.isBlank()) {
                return first.trim();
            }
        }
        return request.getRemoteAddr();
    }
}
