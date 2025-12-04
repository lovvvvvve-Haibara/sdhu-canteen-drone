package com.example.sdhucanteendrone.dto;

import com.example.sdhucanteendrone.entity.enums.UserRole;
import com.example.sdhucanteendrone.entity.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

/**
 * 与“当前用户/用户自身”相关的 DTO 总汇。
 * <p>
 * 内部包含：
 * - 请求体 DTO（带参数校验注解，用于接口安全）
 * - 响应 DTO（UserDetail）
 */
public class SelfUser {

    // ======================= 请求体 DTO =======================

    /**
     * 注册请求体
     */
    @Data
    public static class RegisterReq {

        /** 用户名（学号/工号），不能为空 */
        @NotBlank
        @Size(max = 64)
        private String username;

        /** 显示名称，不能为空 */
        @NotBlank
        @Size(max = 100)
        private String displayName;

        /** 明文密码，不能为空，长度建议控制 */
        @NotBlank
        @Size(min = 6, max = 64)
        private String password;

        /** 手机号，可以为空，看业务需求 */
        @Size(max = 20)
        private String phone;
    }

    /**
     * 登录请求体
     */
    @Data
    public static class LoginReq {

        /** 用户名或手机号，不能为空 */
        @NotBlank
        private String usernameOrPhone;

        /** 密码，不能为空 */
        @NotBlank
        private String password;
    }

    /**
     * 修改密码请求体
     */
    @Data
    public static class PasswordChangeReq {

        /** 旧密码，不能为空 */
        @NotBlank
        private String oldPassword;

        /** 新密码，不能为空 */
        @NotBlank
        @Size(min = 6, max = 64)
        private String newPassword;
    }

    /**
     * 修改个人资料请求体
     */
    @Data
    public static class UpdateProfileReq {

        /** 新的显示名称，不能为空 */
        @NotBlank
        @Size(max = 100)
        private String displayName;

        /** 新手机号，可选 */
        @Size(max = 20)
        private String phone;
    }

    // ======================= 响应 DTO =======================

    /**
     * 用户详情响应体
     * <p>
     * 用于登录成功、注册成功、获取个人信息等接口返回。
     */
    @Data
    public static class UserDetail {

        private Long id;                // 用户ID
        private String username;        // 用户名（原始）
        private String displayName;     // 显示名称
        private UserRole role;          // 用户角色
        private UserStatus status;      // 用户状态
        private String phone;           // 绑定手机号

        private Instant createdAt;      // 创建时间
        private Instant updatedAt;      // 更新时间
    }
}
