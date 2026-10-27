package com.crisp.demo.infrastructure.web;

import com.crisp.demo.shared.security.JwtPayload;
import com.crisp.demo.shared.security.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 第一道拦截器：认证（你是谁）
 *
 * 职责边界：
 * 1) 从 {@code Authorization: Bearer <token>} 取令牌并校验，通过后把用户放进 request 属性，
 *    {@code @CurrentUser} 依旧从同名属性取值，业务代码一行都不用改；
 * 2) 未带令牌 / 令牌非法 / 令牌过期，对受保护接口统一返回 401；
 * 3) 公开接口（列表、详情等 GET）即使没带令牌也放行，带了有效令牌则照样注入用户，
 *    这样首页能显示"欢迎，某某"，而匿名访客不会被挡在门外；
 * 4) 令牌无效但访问的是公开接口时，按匿名处理而不是 401 —— 避免用户令牌过期后连浏览都被打断。
 *
 * 本类不判断角色，角色由第二道 {@link RoleInterceptor} 负责，两者顺序在 WebConfig 中固定。
 */
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthInterceptor.class);

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";
    private static final String ATTR_CURRENT_USER = "currentUser";

    private final JwtService jwtService;

    public JwtAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 0) CORS 预检直接放行，浏览器不会给预检请求带 Authorization
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        boolean publicEndpoint = isPublicGet(uri, request.getMethod());

        // 1) 有令牌就解析；失败按"未登录"处理，是否 401 取决于接口是否公开
        String token = extractToken(request);
        if (token != null) {
            try {
                JwtPayload payload = jwtService.parse(token);
                request.setAttribute(ATTR_CURRENT_USER, payload.toUser());
                return true;
            } catch (ExpiredJwtException e) {
                if (!publicEndpoint) {
                    return AuthResponses.unauthorized(response, "登录已过期，请重新登录");
                }
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("令牌校验失败 uri={} reason={}", uri, e.getMessage());
                if (!publicEndpoint) {
                    return AuthResponses.unauthorized(response, "登录凭证无效，请重新登录");
                }
            }
        }

        // 2) 公开接口：匿名放行
        if (publicEndpoint) {
            return true;
        }

        // 3) 其余接口一律要求登录
        return AuthResponses.unauthorized(response, "请先登录");
    }

    /** 只接受标准 Bearer 头，避免把 Cookie、查询参数等非标准来源当成凭证 */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            return null;
        }
        String token = header.substring(PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    /**
     * 匿名可访问的 GET 端点
     *
     * 说明：这里只列"读"接口。写接口（报名、入社、发活动等）一律需要登录，
     * 因此无需在此登记，新增公开接口时必须同步这里，否则会被 401 挡住。
     *
     * 名单类接口（社团成员 /api/club-member/club/*、报名名单 /api/registration/activity/*）
     * 含真实姓名与学号，匿名遍历 id 即可拉取全校名单，故一律要求登录；
     * /api/registration/check 除外 —— 它对匿名返回 null（视为未报名），不泄露任何个人信息。
     */
    private boolean isPublicGet(String uri, String method) {
        if (!"GET".equalsIgnoreCase(method)) {
            return false;
        }
        if (uri.equals("/api/club/list") || uri.equals("/api/club/search")) return true;
        if (uri.matches("/api/club/\\d+")) return true;
        if (uri.equals("/api/activity/list") || uri.equals("/api/activity/search")) return true;
        if (uri.matches("/api/activity/\\d+")) return true;
        if (uri.startsWith("/api/activity/status/")) return true;
        if (uri.startsWith("/api/activity/club/")) return true;

        if (uri.equals("/api/club-category/list")) return true;
        if (uri.equals("/api/announcement/list")) return true;
        if (uri.matches("/api/announcement/\\d+")) return true;
        if (uri.equals("/api/club-showcase/list")) return true;
        if (uri.matches("/api/club-showcase/club/\\d+")) return true;
        if (uri.matches("/api/club-showcase/\\d+")) return true;

        return uri.equals("/api/registration/check");
    }
}
