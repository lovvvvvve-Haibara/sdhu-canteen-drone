package com.example.sdhucanteendrone.dto;

import com.example.sdhucanteendrone.entity.enums.CanteenOpenStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 食堂相关 DTO
 *
 * 与 User 模块同风格，使用静态内嵌类组织结构，便于 Controller 使用。
 */
public class CanteenDto {

    // ============================================================
    // 食堂基础信息（列表）
    // ============================================================

    @Data
    public static class CanteenSummary {
        private Long id;
        private String name;
        private String location;
        private CanteenOpenStatus openStatus;
    }

    // ============================================================
    // 食堂详情（含管理员列表）
    // ============================================================

    @Data
    public static class CanteenDetail {
        private Long id;
        private String name;
        private String location;
        private CanteenOpenStatus openStatus;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private List<CanteenManagerInfo> managers; // 管理员列表
    }

    // ============================================================
    // DTO：创建食堂
    // ============================================================

    @Data
    public static class CanteenCreateReq {
        private String name;
        private String location;
    }

    // ============================================================
    // DTO：更新食堂信息
    // ============================================================

    @Data
    public static class CanteenUpdateReq {
        private String name;
        private String location;
        private CanteenOpenStatus openStatus; // 可选，也可以在独立接口中修改
    }

    // ============================================================
    // 管理员信息
    // ============================================================

    @Data
    public static class CanteenManagerInfo {
        private Long userId;
        private String username;
        private String displayName;
        private String phone;
    }

    // ============================================================
    // 管理员列表
    // ============================================================

    @Data
    public static class CanteenManagerList {
        private Long canteenId;
        private List<CanteenManagerInfo> managers;
    }
}
