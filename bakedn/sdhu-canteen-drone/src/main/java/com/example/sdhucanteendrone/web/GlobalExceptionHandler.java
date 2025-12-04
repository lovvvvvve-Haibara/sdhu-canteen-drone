package com.example.sdhucanteendrone.web;

import com.example.sdhucanteendrone.Common.ApiResponse;
import com.example.sdhucanteendrone.Common.BizException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBiz(BizException e){
        // ApiResponse.fail 可根据 BizException 携带的 code 设置 HTTP 状态码（若你有该能力）
        return ApiResponse.fail(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidate(MethodArgumentNotValidException e){
        var msg = e.getBindingResult().getFieldError();
        return ApiResponse.fail(msg != null ? msg.getDefaultMessage() : "参数不合法");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOther(Exception e){
        e.printStackTrace();
        return ApiResponse.fail("未知错误");
    }
}
