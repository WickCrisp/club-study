package com.crisp.demo.infrastructure.web;

import com.crisp.demo.shared.error.BusinessException;
import com.crisp.demo.shared.error.ForbiddenException;
import com.crisp.demo.shared.error.TooManyRequestsException;
import com.crisp.demo.shared.error.UnauthorizedException;
import com.crisp.demo.shared.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理 — 消除 Controller 层重复 try-catch
 *
 * 统一约定：响应体始终是 Result{code,message,data}，HTTP 状态码与之保持一致
 *   业务校验失败 → 400，权限不足 → 403，路径不存在 → 404，方法不支持 → 405，未捕获异常 → 500
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常（名额已满、重复报名、状态非法等）→ 400 */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(400, e.getMessage());
    }

    /** 请求过于频繁 → 429 */
    @ExceptionHandler(TooManyRequestsException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<?> handleTooManyRequests(TooManyRequestsException e) {
        return Result.error(429, e.getMessage());
    }

    /** 未认证 → 401（与拦截器直接写出的响应保持同一结构） */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleUnauthorized(UnauthorizedException e) {
        return Result.error(401, e.getMessage());
    }

    /** 权限不足 → 403 */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleForbidden(ForbiddenException e) {
        return Result.error(403, e.getMessage());
    }

    /** 参数校验失败 → 400 + 字段级错误信息 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.error(400, msg);
    }

    /** 路径不存在 → 404（不再被兜底异常吞成 500） */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNotFound(Exception e) {
        return Result.error(404, "接口不存在：" + e.getMessage());
    }

    /** 请求方法不支持 → 405 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.error(405, "请求方法不支持：" + e.getMethod());
    }

    /** 其他未捕获异常 → 500（记录日志，便于排查） */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("未捕获异常", e);
        return Result.error(500, "系统错误");
    }
}
