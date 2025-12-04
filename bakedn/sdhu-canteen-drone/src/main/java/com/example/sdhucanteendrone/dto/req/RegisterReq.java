package com.example.sdhucanteendrone.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求参数。
 * <p>
 * 用于接收前端提交的注册信息。
 */
@Data
public class RegisterReq {

    /** 用户名（学号或工号） */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度需在 3~50 个字符之间")
    private String username;

    /** 用户显示名（昵称） */
    @NotBlank(message = "显示名不能为空")
    @Size(min = 1, max = 100, message = "显示名长度需在 1~100 个字符之间")
    private String displayName;

    /** 密码（明文，后端加密保存） */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度不能少于 6 个字符")
    private String password;
}