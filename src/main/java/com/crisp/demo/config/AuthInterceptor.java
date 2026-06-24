package com.crisp.demo.config;

import com.crisp.demo.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器 — 统一 session key "user"，前后台共享同一登录态
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 1) 公开 GET 端点 — 无需认证
        if ("GET".equalsIgnoreCase(method) && isPublicGet(uri)) return true;

        // 2) 登录/登出/状态检查 — 不强制认证
        if (isSessionCheck(uri, method)) return true;

        // 3) 需要认证 — 统一读 "user"
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
            return false;
        }
        request.setAttribute("currentUser", user);
        return true;
    }

    private boolean isSessionCheck(String uri, String method) {
        if (uri.equals("/api/user/logout") || uri.equals("/api/admin/logout")) return true;
        if ("GET".equalsIgnoreCase(method)) {
            return uri.equals("/api/user/current") || uri.equals("/api/admin/current");
        }
        return false;
    }

    private boolean isPublicGet(String uri) {
        if (uri.equals("/api/club/list") || uri.equals("/api/club/search")) return true;
        if (uri.matches("/api/club/\\d+")) return true;
        if (uri.matches("/api/club-member/club/\\d+")) return true;
        if (uri.equals("/api/activity/list") || uri.equals("/api/activity/search")) return true;
        if (uri.matches("/api/activity/\\d+")) return true;
        if (uri.startsWith("/api/activity/status/")) return true;
        if (uri.startsWith("/api/activity/club/")) return true;

        if (uri.equals("/api/summary/list") || uri.startsWith("/api/summary/")) return true;
        if (uri.equals("/api/registration/check")) return true;
        if (uri.startsWith("/api/registration/activity/")) return true;
        return false;
    }
}
