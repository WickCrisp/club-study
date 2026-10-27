package com.crisp.demo.infrastructure.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 安全响应头
 *
 * 每个响应都带这几条，浏览器侧能挡掉一大类低成本的攻击：
 *   - X-Content-Type-Options: nosniff   禁止浏览器嗅探 MIME，防止 JSON 被当 HTML/JS 执行
 *   - X-Frame-Options: DENY             禁止被 iframe 嵌套，防点击劫持
 *   - Strict-Transport-Security         HSTS；浏览器在 HTTP 下忽略此头，上 HTTPS 后立即生效
 *   - Referrer-Policy                   跳转到外站时不泄露完整 URL（含可能的查询参数）
 *
 * /api/** 另加 Cache-Control: no-store：成员名单、个人资料这类响应
 * 不应落在浏览器或中间代理的缓存里，防止共用电脑/共享代理下的信息残留。
 *
 * 注意：Content-Security-Policy 不在这里设置——CSP 要与实际提供 HTML 的服务匹配，
 * 本服务只出 JSON（页面在 Nginx 上），CSP 放 Nginx 配置里更合适。
 */
@Component
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("X-Frame-Options", "DENY");
        res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        if (req.getRequestURI().startsWith("/api/")) {
            res.setHeader("Cache-Control", "no-store");
        }

        chain.doFilter(request, response);
    }
}
