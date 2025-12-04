package com.example.sdhucanteendrone.Common;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;    // 是否成功
    private String message;     // 错误信息
    private T data;             // 返回数据内容

    public static <T> ApiResponse<T> ok(T data){
        ApiResponse<T> r=new ApiResponse<>();
        r.success=true;
        r.data=data;
        return r;
    }
    public static <T> ApiResponse<T> fail(String msg){
        ApiResponse<T> r=new ApiResponse<>();
        r.success=false;
        r.message=msg;
        return r;
    }
}
