package com.example.sdhucanteendrone.service;

import com.example.sdhucanteendrone.dto.SelfUser;
import com.example.sdhucanteendrone.entity.enums.UserRole;
import com.example.sdhucanteendrone.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 用户相关业务接口。
 * <p>
 * 该接口在 UserRepository 的基础上，提供：
 * - 注册 / 登录
 * - 用户信息查询
 * - 个人资料修改
 * - 密码修改 / 重置
 * - 管理员用户管理（状态变更、列表查询）
 */
public interface UserService {

    // ============================================================
    //   注册 / 登录
    // ============================================================

    /**
     * 用户注册。
     * <p>
     * 一般流程：
     * 1. 将用户名转为小写，检查是否已存在（基于 username_lc 唯一性）
     * 2. 对明文密码进行加密（如 BCrypt）
     * 3. 保存用户信息，默认状态 ACTIVE、角色 CUSTOMER（或按业务约定）
     *
     * @param username    用户名（学号 / 工号）
     * @param displayName 显示名称 / 昵称
     * @param password    明文密码（在 Service 内部进行加密）
     * @return 注册成功后的用户详情
     */
    SelfUser.UserDetail register(String username, String displayName, String password);

    /**
     * 用户登录。
     * <p>
     * 一般流程：
     * 1. 根据小写用户名（或手机号）查询用户
     * 2. 校验密码是否正确
     * 3. 校验用户状态是否允许登录（如：是否被封禁）
     *
     * @param usernameOrPhone 用户名（学号 / 工号）或手机号
     * @param password        明文密码
     * @return 登录成功后的用户详情
     */
    SelfUser.UserDetail login(String usernameOrPhone, String password);


    // ============================================================
    //   个人信息 / 用户查询
    // ============================================================

    /**
     * 根据用户ID查询用户详情。
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    SelfUser.UserDetail getUserDetail(Long userId);

    /**
     * 根据用户名查询用户详情。
     *
     * @param username 用户名（原始）
     * @return 若存在返回用户详情；否则返回 Optional.empty()
     */
    Optional<SelfUser.UserDetail> findByUsername(String username);

    /**
     * 根据小写用户名查询用户详情。
     *
     * @param usernameLc 小写用户名
     * @return 若存在返回用户详情；否则返回 Optional.empty()
     */
    Optional<SelfUser.UserDetail> findByUsernameLc(String usernameLc);

    /**
     * 根据手机号查询用户详情。
     *
     * @param phone 手机号
     * @return 若存在返回用户详情；否则返回 Optional.empty()
     */
    Optional<SelfUser.UserDetail> findByPhone(String phone);


    // ============================================================
    //   唯一性校验（注册 / 修改资料 时使用）
    // ============================================================

    /**
     * 检查用户名（忽略大小写）是否已被占用。
     *
     * @param username 用户名（原始，内部会转小写判断）
     * @return true 表示已存在；false 表示可以使用
     */
    boolean isUsernameTaken(String username);

    /**
     * 检查手机号是否已被占用。
     *
     * @param phone 手机号
     * @return true 表示已存在；false 表示可以使用
     */
    boolean isPhoneTaken(String phone);


    // ============================================================
    //   个人资料修改
    // ============================================================

    /**
     * 修改当前用户的显示名称。
     *
     * @param userId      用户ID
     * @param displayName 新显示名称
     * @return 修改后的用户详情
     */
    SelfUser.UserDetail updateDisplayName(Long userId, String displayName);

    /**
     * 修改当前用户绑定的手机号。
     *
     * @param userId 用户ID
     * @param phone  新手机号
     * @return 修改后的用户详情
     */
    SelfUser.UserDetail updatePhone(Long userId, String phone);


    // ============================================================
    //   密码相关
    // ============================================================

    /**
     * 用户修改密码（需要校验旧密码）。
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文，在 Service 内部加密后存储）
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 管理员重置用户密码（不校验旧密码）。
     *
     * @param userId      用户ID
     * @param newPassword 新密码（明文）
     */
    void resetPassword(Long userId, String newPassword);


    // ============================================================
    //   后台管理：用户列表 / 状态变更
    // ============================================================

    /**
     * 分页查询用户列表，可按角色和状态筛选。
     *
     * @param role     角色（可为 null 表示不按角色过滤）
     * @param status   状态（可为 null 表示不按状态过滤）
     * @param pageable 分页参数
     * @return 用户详情分页结果
     */
    Page<SelfUser.UserDetail> listUsers(UserRole role, UserStatus status, Pageable pageable);

    /**
     * 根据关键字搜索用户（用户名 / 显示名 模糊查询）。
     *
     * @param keyword  搜索关键字
     * @param pageable 分页参数
     * @return 用户详情分页结果
     */
    Page<SelfUser.UserDetail> searchUsers(String keyword, Pageable pageable);

    /**
     * 修改用户状态（如：封禁 / 解封 / 禁用）。
     *
     * @param userId 用户ID
     * @param status 新状态
     */
    void changeUserStatus(Long userId, UserStatus status);

    void changeUserRole(Long userId, UserRole role);
}
