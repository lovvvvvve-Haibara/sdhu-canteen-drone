package com.example.sdhucanteendrone.controller;

import com.example.sdhucanteendrone.Common.Result;
import com.example.sdhucanteendrone.dto.MenuDto;
import com.example.sdhucanteendrone.entity.enums.FoodShelfStatus;
import com.example.sdhucanteendrone.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 菜单（分类 + 菜品）相关接口
 *
 * - 普通用户可以查看指定食堂的菜单
 * - 食堂管理员可以维护分类与菜品
 */
@RestController
@RequestMapping("/api/canteens/{canteenId}/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // ============================================================
    // 分类管理
    // ============================================================

    /**
     * 获取指定食堂的全部分类列表（按 sort 排序）
     */
    @GetMapping("/categories")
    public Result<MenuDto.CategoryList> listCategories(
            @PathVariable("canteenId") Long canteenId) {
        return Result.success(
                menuService.listCategories(canteenId)
        );
    }

    /**
     * 新增分类
     */
    @PostMapping("/categories")
    public Result<MenuDto.CategoryDetail> createCategory(
            @PathVariable("canteenId") Long canteenId,
            @Valid @RequestBody MenuDto.CategoryCreateReq req) {
        return Result.success(
                menuService.createCategory(canteenId, req)
        );
    }

    /**
     * 更新分类（名称 / sort）
     */
    @PatchMapping("/categories/{categoryId}")
    public Result<MenuDto.CategoryDetail> updateCategory(
            @PathVariable("canteenId") Long canteenId,
            @PathVariable("categoryId") Long categoryId,
            @Valid @RequestBody MenuDto.CategoryUpdateReq req) {
        return Result.success(
                menuService.updateCategory(canteenId, categoryId, req)
        );
    }

    /**
     * 删除分类（可要求分类下无菜品时才能删除）
     */
    @DeleteMapping("/categories/{categoryId}")
    public Result<Void> deleteCategory(@PathVariable("canteenId") Long canteenId,
                                       @PathVariable("categoryId") Long categoryId) {
        menuService.deleteCategory(canteenId, categoryId);
        return Result.success();
    }

    // ============================================================
    // 菜品管理
    // ============================================================

    /**
     * 分页查询菜品列表
     *
     * 支持：
     * - 按分类过滤
     * - 按上架状态过滤
     * - 按关键字搜索（菜名）
     */
    @GetMapping("/foods")
    public Result<Page<MenuDto.FoodSummary>> listFoods(
            @PathVariable("canteenId") Long canteenId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) FoodShelfStatus shelfStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<MenuDto.FoodSummary> resultPage = menuService.listFoods(
                canteenId, categoryId, shelfStatus, keyword, PageRequest.of(page, size)
        );
        return Result.success(resultPage);
    }

    /**
     * 获取单个菜品详情
     */
    @GetMapping("/foods/{foodId}")
    public Result<MenuDto.FoodDetail> getFood(
            @PathVariable("canteenId") Long canteenId,
            @PathVariable("foodId") Long foodId) {
        return Result.success(
                menuService.getFoodDetail(canteenId, foodId)
        );
    }

    /**
     * 新增菜品
     */
    @PostMapping("/foods")
    public Result<MenuDto.FoodDetail> createFood(
            @PathVariable("canteenId") Long canteenId,
            @Valid @RequestBody MenuDto.FoodCreateReq req) {
        return Result.success(
                menuService.createFood(canteenId, req)
        );
    }

    /**
     * 修改菜品（名称 / 价格 / 分类 / 图片等）
     */
    @PatchMapping("/foods/{foodId}")
    public Result<MenuDto.FoodDetail> updateFood(
            @PathVariable("canteenId") Long canteenId,
            @PathVariable("foodId") Long foodId,
            @Valid @RequestBody MenuDto.FoodUpdateReq req) {
        return Result.success(
                menuService.updateFood(canteenId, foodId, req)
        );
    }

    /**
     * 设置菜品上下架
     */
    @PatchMapping("/foods/{foodId}/shelf")
    public Result<Void> changeFoodShelfStatus(
            @PathVariable("canteenId") Long canteenId,
            @PathVariable("foodId") Long foodId,
            @RequestParam("status") FoodShelfStatus status) {
        menuService.changeFoodShelfStatus(canteenId, foodId, status);
        return Result.success();
    }

    /**
     * 修改库存（例如盘点 / 批量入库）
     */
    @PatchMapping("/foods/{foodId}/stock")
    public Result<Void> updateFoodStock(
            @PathVariable("canteenId") Long canteenId,
            @PathVariable("foodId") Long foodId,
            @RequestParam("stock") Integer stock) {
        menuService.updateFoodStock(canteenId, foodId, stock);
        return Result.success();
    }
}
