package com.example.sdhucanteendrone.entity.enums;

/**
 * 菜品上下架状态
 *
 * 对 foods.on_shelf 字段提供更语义化的封装。
 * 映射关系：
 *  - ON  -> on_shelf = 1
 *  - OFF -> on_shelf = 0
 */
public enum FoodShelfStatus {
    ON,     // 上架中（可售）
    OFF     // 下架（不可售）
}
