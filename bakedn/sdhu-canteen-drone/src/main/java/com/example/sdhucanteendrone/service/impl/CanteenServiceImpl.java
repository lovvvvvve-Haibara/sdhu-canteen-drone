package com.example.sdhucanteendrone.service.impl;

import com.example.sdhucanteendrone.Common.BizException;
import com.example.sdhucanteendrone.dto.CanteenDto;
import com.example.sdhucanteendrone.entity.Canteen;
import com.example.sdhucanteendrone.entity.CanteenManager;
import com.example.sdhucanteendrone.entity.User;
import com.example.sdhucanteendrone.entity.enums.CanteenOpenStatus;
import com.example.sdhucanteendrone.entity.enums.UserRole;
import com.example.sdhucanteendrone.repository.CanteenManagerRepository;
import com.example.sdhucanteendrone.repository.CanteenRepository;
import com.example.sdhucanteendrone.repository.UserRepository;
import com.example.sdhucanteendrone.service.CanteenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CanteenServiceImpl implements CanteenService {

    private final CanteenRepository canteenRepository;
    private final CanteenManagerRepository canteenManagerRepository;
    private final UserRepository userRepository;

    // ============================================================
    // 食堂列表 / 详情
    // ============================================================

    @Override
    public Page<CanteenDto.CanteenSummary> listCanteens(CanteenOpenStatus status, Pageable pageable) {
        Page<Canteen> page =
                status == null
                        ? canteenRepository.findAll(pageable)
                        : canteenRepository.findByOpenStatus(status, pageable);

        return page.map(this::toSummary);
    }

    @Override
    public CanteenDto.CanteenDetail getCanteenDetail(Long canteenId) {
        Canteen canteen = canteenRepository.findById(canteenId)
                .orElseThrow(() -> BizException.notFound("食堂不存在"));

        CanteenDto.CanteenDetail dto = toDetail(canteen);
        dto.setManagers(loadManagers(canteenId));
        return dto;
    }

    // ============================================================
    // 创建 / 修改
    // ============================================================

    @Override
    @Transactional
    public CanteenDto.CanteenDetail createCanteen(CanteenDto.CanteenCreateReq req) {
        if (canteenRepository.existsByName(req.getName())) {
            throw BizException.badRequest("食堂名称已存在");
        }

        Canteen c = new Canteen();
        c.setName(req.getName());
        c.setLocation(req.getLocation());
        c.setOpenStatus(CanteenOpenStatus.OPEN);
        c.setCreatedAt(Instant.now());
        c.setUpdatedAt(Instant.now());

        Canteen saved = canteenRepository.save(c);
        CanteenDto.CanteenDetail dto = toDetail(saved);
        dto.setManagers(List.of());

        return dto;
    }

    @Override
    @Transactional
    public CanteenDto.CanteenDetail updateCanteen(Long canteenId, CanteenDto.CanteenUpdateReq req) {
        Canteen c = canteenRepository.findById(canteenId)
                .orElseThrow(() -> BizException.notFound("食堂不存在"));

        if (req.getName() != null && !req.getName().equals(c.getName())) {

            // 名称唯一性校验
            canteenRepository.findByName(req.getName())
                    .filter(other -> !other.getId().equals(canteenId))
                    .ifPresent(other -> {
                        throw BizException.badRequest("食堂名称已存在");
                    });
            c.setName(req.getName());
        }

        if (req.getLocation() != null) c.setLocation(req.getLocation());
        if (req.getOpenStatus() != null) c.setOpenStatus(req.getOpenStatus());

        c.setUpdatedAt(Instant.now());
        Canteen saved = canteenRepository.save(c);

        CanteenDto.CanteenDetail dto = toDetail(saved);
        dto.setManagers(loadManagers(canteenId));
        return dto;
    }

    @Override
    @Transactional
    public void deleteCanteen(Long canteenId) {
        // 校验是否存在
        Canteen c = canteenRepository.findById(canteenId)
                .orElseThrow(() -> BizException.notFound("食堂不存在"));

        // TODO 如果有业务规则（如已绑定档口/窗口/员工），可在这里加校验
        // if (storeRepository.existsByCanteenId(canteenId)) { ... }

        // 执行删除
        canteenRepository.delete(c);
    }

    @Override
    @Transactional
    public void changeCanteenStatus(Long canteenId, CanteenOpenStatus status) {
        int updated = canteenRepository.updateOpenStatusById(canteenId, status);
        if (updated == 0) {
            throw BizException.notFound("食堂不存在");
        }
    }

    // ============================================================
    // 管理员
    // ============================================================

    @Override
    public CanteenDto.CanteenManagerList listCanteenManagers(Long canteenId) {
        if (!canteenRepository.existsById(canteenId)) {
            throw BizException.notFound("食堂不存在");
        }

        CanteenDto.CanteenManagerList dto = new CanteenDto.CanteenManagerList();
        dto.setCanteenId(canteenId);
        dto.setManagers(loadManagers(canteenId));
        return dto;
    }

    @Override
    @Transactional
    public void addCanteenManager(Long canteenId, Long userId) {

        if (!canteenRepository.existsById(canteenId)) {
            throw BizException.notFound("食堂不存在");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BizException.notFound("用户不存在"));

        if (user.getRole() != UserRole.CANTEEN && user.getRole() != UserRole.ADMIN) {
            throw BizException.badRequest("该用户不是食堂管理员角色");
        }

        if (canteenManagerRepository.existsByCanteenIdAndUserId(canteenId, userId)) {
            return; // 已存在，幂等
        }

        CanteenManager cm = new CanteenManager();
        cm.setCanteenId(canteenId);
        cm.setUserId(userId);

        canteenManagerRepository.save(cm);
    }

    @Override
    @Transactional
    public void removeCanteenManager(Long canteenId, Long userId) {
        int deleted = canteenManagerRepository.deleteByCanteenIdAndUserId(canteenId, userId);
        if (deleted == 0) {
            throw BizException.notFound("管理员记录不存在");
        }
    }

    // ============================================================
    // 工具方法
    // ============================================================

    private CanteenDto.CanteenSummary toSummary(Canteen c) {
        CanteenDto.CanteenSummary dto = new CanteenDto.CanteenSummary();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setLocation(c.getLocation());
        dto.setOpenStatus(c.getOpenStatus());
        return dto;
    }

    private CanteenDto.CanteenDetail toDetail(Canteen c) {
        CanteenDto.CanteenDetail dto = new CanteenDto.CanteenDetail();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setLocation(c.getLocation());
        dto.setOpenStatus(c.getOpenStatus());
        dto.setCreatedAt(toLocalDateTime(c.getCreatedAt()));
        dto.setUpdatedAt(toLocalDateTime(c.getUpdatedAt()));
        return dto;
    }

    private List<CanteenDto.CanteenManagerInfo> loadManagers(Long canteenId) {
        List<CanteenManager> cms = canteenManagerRepository.findByCanteenId(canteenId);

        return cms.stream()
                .map(cm -> {
                    User u = userRepository.findById(cm.getUserId())
                            .orElse(null);

                    CanteenDto.CanteenManagerInfo info = new CanteenDto.CanteenManagerInfo();
                    if (u != null) {
                        info.setUserId(u.getId());
                        info.setUsername(u.getUsername());
                        info.setDisplayName(u.getDisplayName());
                        info.setPhone(u.getPhone());
                    }
                    return info;
                })
                .collect(Collectors.toList());
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null :
                LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
