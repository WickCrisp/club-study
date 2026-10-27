package com.crisp.demo.infrastructure.web;

import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.shared.security.RequireRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 第二道拦截器：授权（你能做什么）
 *
 * 只在处理器方法（或所在类）标注了 {@link RequireRole} 时才生效，其余接口直接放行。
 * 这样做的原因：项目里 /api/admin/** 并非全是管理员接口 —— 社团负责人（STUDENT）
 * 也要用管理端发布活动、审批成员，按路径一刀切会把正常业务挡掉。
 *
 * 权限判定只读第一道拦截器放进去的 currentUser，不重复解析令牌。
 */
public class RoleInterceptor implements HandlerInterceptor {

    private static final String ATTR_CURRENT_USER = "currentUser";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole require = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (require == null) {
            require = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (require == null) {
            return true;
        }

        User user = (User) request.getAttribute(ATTR_CURRENT_USER);
        if (user == null) {
            // 正常流程不会走到这里（认证拦截器已挡住），保留作为防御
            return AuthResponses.unauthorized(response, "请先登录");
        }

        String[] allowed = require.value();
        if (!Arrays.asList(allowed).contains(user.getRole())) {
            String roles = String.join("/", allowed);
            String action = require.action();
            String message = action.isEmpty()
                    ? "无权限：该操作仅 " + roles + " 可执行"
                    : "无权限：" + action + "，该操作仅 " + roles + " 可执行";
            return AuthResponses.forbidden(response, message);
        }
        return true;
    }
}
