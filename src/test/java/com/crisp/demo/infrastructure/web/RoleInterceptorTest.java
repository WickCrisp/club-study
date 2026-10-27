package com.crisp.demo.infrastructure.web;

import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.shared.security.RequireRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 第二道拦截器：授权行为
 *
 * 重点验证"按注解判定，而不是按路径一刀切"——没有注解的接口即使当前用户是学生也放行。
 */
class RoleInterceptorTest {

    /** 测试用的处理器：一个标了注解，一个没标 */
    static class SampleController {

        @RequireRole(value = "ADMIN", action = "查看全局统计")
        public void adminOnly() {
        }

        public void anyRole() {
        }
    }

    private RoleInterceptor interceptor;
    private HandlerMethod adminOnly;
    private HandlerMethod anyRole;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        interceptor = new RoleInterceptor();
        SampleController bean = new SampleController();
        adminOnly = new HandlerMethod(bean, SampleController.class.getMethod("adminOnly"));
        anyRole = new HandlerMethod(bean, SampleController.class.getMethod("anyRole"));
    }

    private static MockHttpServletRequest requestWithRole(String role) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/analytics/global");
        User user = new User();
        user.setId(1L);
        user.setRole(role);
        request.setAttribute("currentUser", user);
        return request;
    }

    @Test
    @DisplayName("管理员访问标注 @RequireRole 的接口放行")
    void adminPasses() throws Exception {
        boolean pass = interceptor.preHandle(requestWithRole("ADMIN"), new MockHttpServletResponse(), adminOnly);
        assertTrue(pass);
    }

    @Test
    @DisplayName("学生访问管理员接口 → 403，提示带动作名")
    void studentForbidden() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean pass = interceptor.preHandle(requestWithRole("STUDENT"), response, adminOnly);

        assertFalse(pass);
        assertEquals(403, response.getStatus());
        String body = response.getContentAsString(StandardCharsets.UTF_8);
        assertTrue(body.contains("\"code\":403"));
        assertTrue(body.contains("查看全局统计"));
    }

    @Test
    @DisplayName("未标注注解的接口不做角色限制（社团负责人也要用管理端）")
    void unannotatedEndpointPassesForStudent() throws Exception {
        boolean pass = interceptor.preHandle(requestWithRole("STUDENT"), new MockHttpServletResponse(), anyRole);
        assertTrue(pass);
    }

    @Test
    @DisplayName("没有登录态时兜底返回 401")
    void missingUserReturns401() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean pass = interceptor.preHandle(new MockHttpServletRequest("GET", "/api/analytics/global"),
                response, adminOnly);

        assertFalse(pass);
        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("非控制器方法（如静态资源处理器）直接放行")
    void nonHandlerMethodPasses() throws Exception {
        assertTrue(interceptor.preHandle(requestWithRole("STUDENT"), new MockHttpServletResponse(), new Object()));
    }
}
