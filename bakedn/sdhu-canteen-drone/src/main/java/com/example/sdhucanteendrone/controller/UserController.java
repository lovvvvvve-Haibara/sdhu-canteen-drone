package com.example.sdhucanteendrone.controller;

import com.example.sdhucanteendrone.Common.Result;
import com.example.sdhucanteendrone.dto.SelfUser;
import com.example.sdhucanteendrone.entity.enums.UserRole;
import com.example.sdhucanteendrone.entity.enums.UserStatus;
import com.example.sdhucanteendrone.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 用户相关接口 Controller。
 *
 * 说明：
 * - 所有接口统一返回 Result<T>，方便前端统一处理
 * - 参数校验依赖 @Valid + DTO 上的 @NotBlank 等注解
 * - 业务异常由 BizException 抛出，全局异常处理器会统一包装为 Result
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ============================================================
    //   注册 / 登录
    // ============================================================

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<SelfUser.UserDetail> register(@Valid @RequestBody SelfUser.RegisterReq req) {
        SelfUser.UserDetail detail =
                userService.register(req.getUsername(), req.getDisplayName(), req.getPassword());
        return Result.success(detail);
    }

    /**
     * 用户登录
     * 目前仅返回用户信息，后续如需 JWT，可以在此一起返回 token
     */
    @PostMapping("/login")
    public Result<SelfUser.UserDetail> login(@Valid @RequestBody SelfUser.LoginReq req) {
        SelfUser.UserDetail detail =
                userService.login(req.getUsernameOrPhone(), req.getPassword());
        return Result.success(detail);
    }

    // ============================================================
    //   用户信息（查询）
    // ============================================================

    /**
     * 根据 ID 获取用户详情
     * 可用于后台或通用的“查看用户信息”场景
     */
    @GetMapping("/{id}")
    public Result<SelfUser.UserDetail> getUserById(@PathVariable("id") Long userId) {
        return Result.success(userService.getUserDetail(userId));
    }

    /**
     * 获取“当前用户”信息（临时写法）
     * 目前从参数拿 userId，之后接入登录态后可改为从 SecurityContext 获取。
     */
    @GetMapping("/self")
    public Result<SelfUser.UserDetail> getSelf(@RequestParam("userId") Long userId) {
        return Result.success(userService.getUserDetail(userId));
    }

    // ============================================================
    //   个人资料修改
    // ============================================================

    /**
     * 修改用户资料（昵称 / 手机号）
     *
     * 说明：
     * - 为了简单，调用了两次 Service：updateDisplayName + updatePhone
     * - 若你更追求效率，可以在 Service 中新增一个 updateProfile 方法，一次性更新
     */
    @PatchMapping("/{id}/profile")
    public Result<SelfUser.UserDetail> updateProfile(@PathVariable("id") Long userId,
                                                     @Valid @RequestBody SelfUser.UpdateProfileReq req) {
        // 先更新昵称
        SelfUser.UserDetail updated = userService.updateDisplayName(userId, req.getDisplayName());
        // 如果请求里带了 phone，再更新手机号
        if (req.getPhone() != null) {
            updated = userService.updatePhone(userId, req.getPhone());
        }
        return Result.success(updated);
    }

    /**
     * 修改密码（需要旧密码）
     */
    @PostMapping("/{id}/password/change")
    public Result<Void> changePassword(@PathVariable("id") Long userId,
                                       @Valid @RequestBody SelfUser.PasswordChangeReq req) {
        userService.changePassword(userId, req.getOldPassword(), req.getNewPassword());
        return Result.success(); // 无返回数据，但保持统一结构
    }

    // ============================================================
    //   后台管理：列表 / 搜索 / 状态 / 重置密码
    // ============================================================

    /**
     * 分页查询用户列表（可按角色、状态过滤）
     *
     * @param page   页码（从 0 开始）
     * @param size   每页数量
     * @param role   用户角色（可选）
     * @param status 用户状态（可选）
     */
    @GetMapping
    public Result<Page<SelfUser.UserDetail>> listUsers(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(required = false) UserRole role,
                                                       @RequestParam(required = false) UserStatus status) {
        Page<SelfUser.UserDetail> resultPage =
                userService.listUsers(role, status, PageRequest.of(page, size));
        return Result.success(resultPage);
    }

    /**
     * 关键字搜索用户（对用户名 / 显示名进行模糊匹配）
     */
    @GetMapping("/search")
    public Result<Page<SelfUser.UserDetail>> searchUsers(@RequestParam("keyword") String keyword,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        Page<SelfUser.UserDetail> resultPage =
                userService.searchUsers(keyword, PageRequest.of(page, size));
        return Result.success(resultPage);
    }

    /**
     * 修改用户状态（封禁 / 解封 / 禁用等）
     */
    @PatchMapping("/{id}/status")
    public Result<Void> changeUserStatus(@PathVariable("id") Long userId,
                                         @RequestParam("status") UserStatus status) {
        userService.changeUserStatus(userId, status);
        return Result.success();
    }

    /**
     * 修改用户角色
     *     CUSTOMER,   // 普通顾客
     *     CANTEEN,    // 食堂管理员
     *     ADMIN       // 系统管理员
     */
    @PatchMapping("/{id}/role")
    public Result<Void> changeUserRole(@PathVariable("id") Long userId,
                                       @RequestParam("role") UserRole role) {
        userService.changeUserRole(userId, role);
        return Result.success();
    }
    /**
     * 管理员重置用户密码
     * 通常用于后台手动重置为一个临时密码
     */
    @PostMapping("/{id}/password/reset")
    public Result<Void> resetPassword(@PathVariable("id") Long userId,
                                      @RequestParam("newPassword") String newPassword) {
        userService.resetPassword(userId, newPassword);
        return Result.success();
    }
}
