package com.example.sdhucanteendrone.dto;

import com.example.sdhucanteendrone.entity.enums.DroneStatus;
import lombok.Data;

import java.time.Instant;

/**
 * 无人机相关 DTO
 *
 * 用于 DroneController / DroneService。
 */
public class DroneDto {

    // ============================================================
    // 无人机列表项（DroneSummary）
    // ============================================================

    @Data
    public static class DroneSummary {
        private Long id;
        /** 无人机唯一编号，例如 DRN-001 */
        private String code;
        /** 状态：IDLE / IN_MISSION / MAINTENANCE */
        private DroneStatus status;
        /** 当前电量（0-100） */
        private Integer battery;
        /** 当前位置描述 */
        private String location;
        private Instant createdAt;
        private Instant updatedAt;
    }

    // ============================================================
    // 无人机详情（DroneDetail）
    // ============================================================

    @Data
    public static class DroneDetail {
        private Long id;
        private String code;
        private DroneStatus status;
        private Integer battery;
        private String location;
        private Instant createdAt;
        private Instant updatedAt;
    }

    // ============================================================
    // 创建无人机请求（DroneCreateReq）
    // ============================================================

    @Data
    public static class DroneCreateReq {
        private String code;
        private String model;
        private Double maxPayloadKg;
        private Integer battery;
        private DroneStatus status;
        private String location;
        private String note;
    }


    // ============================================================
    // 更新无人机信息请求（DroneUpdateReq）
    // ============================================================

    @Data
    public static class DroneUpdateReq {

        /** 可修改编号 */
        private String code;

        /** 型号，可选 */
        private String model;

        /** 最大载重（kg），可选 */
        private Double maxPayloadKg;

        /** 电量（百分比 0–100），可选 */
        private Integer battery;

        /** 无人机当前状态（IDLE / IN_MISSION / MAINTENANCE） */
        private DroneStatus status;

        /** 备注信息，可选 */
        private String note;
    }

}
