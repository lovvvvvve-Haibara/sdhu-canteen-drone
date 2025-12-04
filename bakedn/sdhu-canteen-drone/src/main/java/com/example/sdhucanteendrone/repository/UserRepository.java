package com.example.sdhucanteendrone.repository;

import com.example.sdhucanteendrone.entity.User;
import com.example.sdhucanteendrone.entity.enums.UserRole;
import com.example.sdhucanteendrone.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户实体对应的 Repository，用于对 users 表进行增删改查。
 * <p>
 * 该仓储接口基于 Spring Data JPA，提供常用的查询、分页查询、
 * 状态更新、资料更新等方法，满足用户模块的大部分基础需求。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ============================================================
    //   用户基础查询（登录、注册、唯一性校验）
    // ============================================================

    /**
     * 根据原始用户名（可能区分大小写）查询用户。
     * <p>
     * 字段：username
     * <p>
     * 适用场景：
     * - 某些场景需要保留原始大小写，例如展示用途
     * - 仅当需要使用原始用户名查询时使用
     *
     * @param username 原始用户名
     * @return 若存在则返回用户对象，否则返回 Optional.empty()
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据小写用户名（username_lc）查询用户。
     * <p>
     * 字段：usernameLc
     * <p>
     * 适用场景：
     * - 登录（推荐）
     * - 注册时检查是否重复
     * - 用户名统一转小写存入 username_lc，以避免大小写差异导致重复
     *
     * @param usernameLc 小写用户名
     * @return 若存在则返回用户对象，否则返回 Optional.empty()
     */
    Optional<User> findByUsernameLc(String usernameLc);

    /**
     * 判断原始用户名是否存在（区分大小写）。
     * <p>
     * 字段：username
     *
     * @param username 原始用户名
     * @return true 表示已存在；false 表示不存在
     */
    boolean existsByUsername(String username);

    /**
     * 判断小写用户名是否存在（推荐使用该方法进行注册校验）。
     * <p>
     * 字段：usernameLc
     * <p>
     * 使用小写进行唯一性校验可以避免：
     * - Alice 和 alice 被当成两个用户的重复注册问题
     *
     * @param usernameLc 小写用户名
     * @return true 表示已存在；false 表示不存在
     */
    boolean existsByUsernameLc(String usernameLc);

    /**
     * 根据手机号查询用户。
     *
     * @param phone 手机号
     * @return 若存在则返回用户对象，否则返回 Optional.empty()
     */
    Optional<User> findByPhone(String phone);


    // ============================================================
    //   分页 & 条件查询（后台管理常用）
    // ============================================================

    /**
     * 根据角色分页查询用户。
     * <p>
     * 字段：role
     *
     * @param role     角色（CUSTOMER / CANTEEN / ADMIN）
     * @param pageable 分页参数
     * @return 分页数据
     */
    Page<User> findByRole(UserRole role, Pageable pageable);

    /**
     * 根据用户状态分页查询。
     * <p>
     * 字段：status
     *
     * @param status   状态（ACTIVE / LOCKED / DISABLED）
     * @param pageable 分页参数
     * @return 分页数据
     */
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    /**
     * 根据角色和状态组合条件分页查询。
     *
     * @param role     用户角色
     * @param status   用户状态
     * @param pageable 分页参数
     * @return 分页数据
     */
    Page<User> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);

    /**
     * 根据关键字搜索用户（支持用户名和显示名模糊匹配）。
     * <p>
     * 使用 lower() 实现不区分大小写的模糊搜索。
     *
     * @param keyword  搜索关键字
     * @param pageable 分页参数
     * @return 分页数据
     */
    @Query("""
           select u from User u
           where lower(u.username) like lower(concat('%', :keyword, '%'))
              or lower(u.displayName) like lower(concat('%', :keyword, '%'))
           """)
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);


    // ============================================================
    //   用户状态更新 / 资料更新（后台/用户管理）
    // ============================================================

    /**
     * 更新用户状态（如：封禁 / 解封 / 禁用）。
     * <p>
     * 注意：调用此方法需要使用 @Transactional 才能生效。
     *
     * @param id     用户ID
     * @param status 新状态
     * @return 受影响的行数（0 表示未找到用户）
     */
    @Modifying
    @Query("update User u set u.status = :status where u.id = :id")
    int updateStatusById(@Param("id") Long id, @Param("status") UserStatus status);

    /**
     * 更新用户密码（存储加密后的 hash）。
     * <p>
     * 不存储明文密码，只更新 passwordHash 字段。
     * <p>
     * 注意：调用此方法需要 @Transactional。
     *
     * @param id            用户ID
     * @param passwordHash  新密码的哈希值
     * @return 更新影响行数
     */
    @Modifying
    @Query("update User u set u.passwordHash = :passwordHash where u.id = :id")
    int updatePasswordHashById(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    /**
     * 更新用户显示名称（昵称）。
     * <p>
     * 适用于个人资料修改、后台修改用户信息等。
     *
     * @param id          用户ID
     * @param displayName 新显示名
     * @return 更新影响行数
     */
    @Modifying
    @Query("update User u set u.displayName = :displayName where u.id = :id")
    int updateDisplayNameById(@Param("id") Long id, @Param("displayName") String displayName);

    @Query("update User u set u.role = :role where u.id = :id")
    @Modifying
    int updateRoleById(UserRole role, Long id);
}
