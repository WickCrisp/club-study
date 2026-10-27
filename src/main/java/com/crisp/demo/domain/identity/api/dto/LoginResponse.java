package com.crisp.demo.domain.identity.api.dto;

import com.crisp.demo.domain.identity.model.User;

/**
 * 登录响应
 *
 * @param token      JWT，前端存 localStorage 并通过 Authorization: Bearer 回传
 * @param expiresIn  有效期（秒），前端可据此提前提示续期
 * @param user       用户信息，便于登录后立即渲染，省一次 /current 请求
 */
public record LoginResponse(String token, long expiresIn, User user) {
}
