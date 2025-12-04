package com.example.sdhucanteendrone.Common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * 自定义业务异常类（统一抛出业务错误）
 *
 * 使用方式：
 *  throw new BizException(HttpStatus.BAD_REQUEST, "用户名已存在");
 *  throw BizException.notFound("用户不存在");
 *  throw BizException.unauthorized("请先登录");
 *
 * Spring 会自动捕获 ResponseStatusException 并返回对应状态码和 message。
 */
public class BizException extends ResponseStatusException {

    /** 构造函数 */
    public BizException(HttpStatus status, String reason) {
        super(status, reason);
    }

    // 🔹 常用静态方法封装（方便快速抛出）

    public static BizException badRequest(String message) {
        return new BizException(HttpStatus.BAD_REQUEST, message);
    }

    public static BizException notFound(String message) {
        return new BizException(HttpStatus.NOT_FOUND, message);
    }

    public static BizException unauthorized(String message) {
        return new BizException(HttpStatus.UNAUTHORIZED, message);
    }

    public static BizException forbidden(String message) {
        return new BizException(HttpStatus.FORBIDDEN, message);
    }

    public static BizException internalError(String message) {
        return new BizException(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
