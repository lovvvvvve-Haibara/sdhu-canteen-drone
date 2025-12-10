package com.example.sdhucanteendrone.service.impl;

import com.example.sdhucanteendrone.Common.BizException;
import com.example.sdhucanteendrone.dto.SelfUser;
import com.example.sdhucanteendrone.entity.User;
import com.example.sdhucanteendrone.entity.enums.UserRole;
import com.example.sdhucanteendrone.entity.enums.UserStatus;
import com.example.sdhucanteendrone.repository.UserRepository;
import com.example.sdhucanteendrone.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ============================================================
    //   注册
    // ============================================================

    @Override
    @Transactional
    public SelfUser.UserDetail register(String username, String displayName, String password) {

        String usernameLc = username.toLowerCase();
        if (userRepository.existsByUsernameLc(usernameLc)) {
            throw BizException.badRequest("该用户名已被注册");
        }

        User user = new User();
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        return toUserDetail(userRepository.save(user));
    }

    // ============================================================
    //   登录
    // ============================================================

    @Override
    @Transactional
    public SelfUser.UserDetail login(String usernameOrPhone, String password) {


        String usernameLc = usernameOrPhone.toLowerCase();
        Optional<User> optionalUser =
                userRepository.findByUsernameLc(usernameOrPhone)
                        .or(() -> userRepository.findByPhone(usernameOrPhone));
        System.out.println(usernameLc);
        System.out.println(password);
        User user = optionalUser.orElseThrow(
                () -> BizException.unauthorized("用户不存在")
        );

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw BizException.unauthorized("密码错误");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw BizException.forbidden("当前用户状态不允许登录：" + user.getStatus());
        }

        return toUserDetail(user);
    }

    // ============================================================
    //   用户信息查询
    // ============================================================

    @Override
    public SelfUser.UserDetail getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BizException.notFound("用户不存在"));
        return toUserDetail(user);
    }

    @Override
    public Optional<SelfUser.UserDetail> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::toUserDetail);
    }

    @Override
    public Optional<SelfUser.UserDetail> findByUsernameLc(String usernameLc) {
        return userRepository.findByUsernameLc(usernameLc).map(this::toUserDetail);
    }

    @Override
    public Optional<SelfUser.UserDetail> findByPhone(String phone) {
        return userRepository.findByPhone(phone).map(this::toUserDetail);
    }

    // ============================================================
    //   唯一性校验
    // ============================================================

    @Override
    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsernameLc(username.toLowerCase());
    }

    @Override
    public boolean isPhoneTaken(String phone) {
        return userRepository.findByPhone(phone).isPresent();
    }

    // ============================================================
    //   资料修改
    // ============================================================

    @Override
    @Transactional
    public SelfUser.UserDetail updateDisplayName(Long userId, String displayName) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BizException.notFound("用户不存在"));

        user.setDisplayName(displayName);
        user.setUpdatedAt(Instant.now());

        return toUserDetail(userRepository.save(user));
    }

    @Override
    @Transactional
    public SelfUser.UserDetail updatePhone(Long userId, String phone) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BizException.notFound("用户不存在"));

        if (phone != null && !phone.isBlank()) {
            userRepository.findByPhone(phone)
                    .filter(u -> !u.getId().equals(userId))
                    .ifPresent(u -> {
                        throw BizException.badRequest("该手机号已被占用");
                    });
        }

        user.setPhone(phone);
        user.setUpdatedAt(Instant.now());

        return toUserDetail(userRepository.save(user));
    }

    // ============================================================
    //   密码修改
    // ============================================================

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BizException.notFound("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw BizException.badRequest("旧密码不正确");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {

        int updated = userRepository.updatePasswordHashById(
                userId, passwordEncoder.encode(newPassword)
        );

        if (updated == 0) {
            throw BizException.notFound("用户不存在");
        }
    }

    // ============================================================
    //   后台管理
    // ============================================================

    @Override
    public Page<SelfUser.UserDetail> listUsers(UserRole role, UserStatus status, Pageable pageable) {
        Page<User> page;

        if (role != null && status != null) {
            page = userRepository.findByRoleAndStatus(role, status, pageable);
        } else if (role != null) {
            page = userRepository.findByRole(role, pageable);
        } else if (status != null) {
            page = userRepository.findByStatus(status, pageable);
        } else {
            page = userRepository.findAll(pageable);
        }

        return page.map(this::toUserDetail);
    }

    @Override
    public Page<SelfUser.UserDetail> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchByKeyword(keyword == null ? "" : keyword, pageable)
                .map(this::toUserDetail);
    }

    @Override
    @Transactional
    public void changeUserStatus(Long userId, UserStatus status) {
        int updated = userRepository.updateStatusById(userId, status);
        if (updated == 0) {
            throw BizException.notFound("用户不存在");
        }
    }
    @Override
    @Transactional
    public void changeUserRole(Long userId, UserRole role){
        int updated = userRepository.updateRoleById(role,userId);
        if (updated == 0) {
            throw BizException.notFound("用户不存在");
        }
    }

    // ============================================================
    //   工具方法：实体 -> DTO
    // ============================================================

    private SelfUser.UserDetail toUserDetail(User user) {
        SelfUser.UserDetail dto = new SelfUser.UserDetail();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setPhone(user.getPhone());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
