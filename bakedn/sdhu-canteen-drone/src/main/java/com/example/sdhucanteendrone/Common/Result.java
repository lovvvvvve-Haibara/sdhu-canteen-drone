package com.example.sdhucanteendrone.Common;

import lombok.Data;

/**
 * 通用响应包装类
 *
 * 一般约定：
 * - code == 0 表示成功
 * - code != 0 表示业务或系统异常
 */
@Data
public class Result<T> {

    /** 业务状态码：0 表示成功，其它表示失败 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 具体业务数据 */
    private T data;

    // ==================== 工厂方法 ====================

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(0);
        r.setMessage("OK");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> failure(int code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        r.setData(null);
        return r;
    }

    public static <T> Result<T> failure(String message) {
        return failure(-1, message);
    }
}
