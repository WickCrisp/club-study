package com.crisp.demo.infrastructure.web;

import com.crisp.demo.domain.identity.model.User;
import com.crisp.demo.shared.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 第一道拦截器：认证行为
 *
 * 关注三件事：受保护接口没带令牌必须 401、公开接口匿名可进、令牌无效不能"降级放行"。
 */
class JwtAuthInterceptorTest {

    private static final String SECRET = "unit-test-secret-key-at-least-32-bytes-long-2026";

    private JwtService jwtService;
    private JwtAuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 120);
        interceptor = new JwtAuthInterceptor(jwtService);
    }

    private static User sampleUser() {
        User user = new User();
        user.setId(9L);
        user.setStudentNo("202335710341");
        user.setName("李思远");
        user.setRole("STUDENT");
        return user;
    }

    private static MockHttpServletRequest request(String method, String uri) {
        return new MockHttpServletRequest(method, uri);
    }

    @Test
    @DisplayName("受保护接口无令牌 → 401，响应体 code 与 HTTP 状态一致")
    void missingTokenOnProtectedEndpoint() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean pass = interceptor.preHandle(request("POST", "/api/club"), response, new Object());

        assertFalse(pass);
        assertEquals(401, response.getStatus());
        String body = response.getContentAsString(StandardCharsets.UTF_8);
        assertTrue(body.contains("\"code\":401"));
        assertTrue(body.contains("请先登录"));
    }

    @Test
    @DisplayName("公开 GET 接口匿名可访问")
    void publicGetAllowsAnonymous() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/activity/list");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean pass = interceptor.preHandle(request, response, new Object());

        assertTrue(pass);
        assertEquals(200, response.getStatus());
        assertNull(request.getAttribute("currentUser"));
    }

    @Test
    @DisplayName("携带有效令牌时注入当前用户")
    void validTokenInjectsUser() throws Exception {
        String token = jwtService.issue(sampleUser());
        MockHttpServletRequest request = request("POST", "/api/registration/activity/1");
        request.addHeader("Authorization", "Bearer " + token);

        boolean pass = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertTrue(pass);
        User injected = (User) request.getAttribute("currentUser");
        assertNotNull(injected);
        assertEquals(9L, injected.getId().longValue());
        assertEquals("STUDENT", injected.getRole());
    }

    @Test
    @DisplayName("伪造令牌访问受保护接口 → 401")
    void invalidTokenOnProtectedEndpoint() throws Exception {
        MockHttpServletRequest request = request("PUT", "/api/club/1");
        request.addHeader("Authorization", "Bearer not-a-real-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean pass = interceptor.preHandle(request, response, new Object());

        assertFalse(pass);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString(StandardCharsets.UTF_8).contains("登录凭证无效"));
    }

    @Test
    @DisplayName("令牌无效但访问公开接口 → 按匿名放行，不打断浏览")
    void invalidTokenOnPublicEndpointStillBrowsable() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/club/list");
        request.addHeader("Authorization", "Bearer expired-or-broken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean pass = interceptor.preHandle(request, response, new Object());

        assertTrue(pass);
        assertNull(request.getAttribute("currentUser"));
    }

    @Test
    @DisplayName("CORS 预检请求不带令牌，直接放行")
    void optionsRequestPasses() throws Exception {
        boolean pass = interceptor.preHandle(request("OPTIONS", "/api/club"), new MockHttpServletResponse(), new Object());
        assertTrue(pass);
    }
}
