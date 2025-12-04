package com.example.sdhucanteendrone.entity.enums;

/**
 * 食堂营业状态枚举
 *
 * 对应数据库字段：
 *  - OPEN   ：正常营业
 *  - CLOSED ：暂停营业 / 维修 / 关门
 */
public enum CanteenOpenStatus {
    OPEN,       // 营业中
    CLOSED      // 未营业（休息、停业）
}
