package com.example.sdhucanteendrone.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单（分类 + 菜品）相关 DTO
 *
 * 建议与 MenuController / MenuService 搭配使用：
 * - Category* 系列：菜单分类
 * - Food* 系列：菜品 / SKU
 */
public class MenuDto {

    // ============================================================
    // 分类相关 DTO
    // ============================================================

    /**
     * 分类列表包装（便于未来扩展例如附带食堂名称等）
     */
    @Data
    public static class CategoryList {
        private Long canteenId;
        private List<CategorySummary> categories;
    }

    /**
     * 分类列表项（简要信息，用于列表展示）
     */
    @Data
    public static class CategorySummary {
        private Long id;
        private Long canteenId;
        private String name;
        private Integer sort;
    }

    /**
     * 分类详情（目前和 Summary 差不多，预留 createdAt / updatedAt 等）
     */
    @Data
    public static class CategoryDetail {
        private Long id;
        private Long canteenId;
        private String name;
        private Integer sort;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * 创建分类请求
     */
    @Data
    public static class CategoryCreateReq {
        private String name;
        /**
         * 排序值（越小越靠前），前端可不传则在 Service 里给默认值
         */
        private Integer sort;
    }

    /**
     * 更新分类请求
     */
    @Data
    public static class CategoryUpdateReq {
        private String name;
        private Integer sort;
    }

    // ============================================================
    // 菜品相关 DTO
    // ============================================================

    /**
     * 菜品列表项（简要信息）
     *
     * 对应表：foods
     * 字段：id, canteen_id, category_id, name, price_cent, stock, on_shelf, image_url 等
     */
    @Data
    public static class FoodSummary {
        private Long id;
        private Long canteenId;
        private Long categoryId;

        private String categoryName;   // 方便前端展示
        private String name;

        /**
         * 单价（分），对应 foods.price_cent
         */
        private Integer priceCent;

        /**
         * 库存，对应 foods.stock
         */
        private Integer stock;

        /**
         * 是否上架，对应 foods.on_shelf（TINYINT 1/0）
         */
        private Boolean onShelf;

        private String imageUrl;
    }

    /**
     * 菜品详情
     */
    @Data
    public static class FoodDetail {
        private Long id;
        private Long canteenId;
        private Long categoryId;

        private String categoryName;
        private String name;

        private Integer priceCent;
        private Integer stock;
        private Boolean onShelf;
        private String imageUrl;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    /**
     * 创建菜品请求
     */
    @Data
    public static class FoodCreateReq {
        private Long categoryId;   // 可为空：表示“未分类”
        private String name;

        /**
         * 单价（分）
         */
        private Integer priceCent;

        /**
         * 初始库存
         */
        private Integer stock;

        private String imageUrl;

        /**
         * 是否上架；可不传则在 Service 中默认 true
         */
        private Boolean onShelf;
    }

    /**
     * 更新菜品请求
     */
    @Data
    public static class FoodUpdateReq {
        private Long categoryId;
        private String name;

        private Integer priceCent;
        private Integer stock;

        private String imageUrl;
        private Boolean onShelf;
    }
}
