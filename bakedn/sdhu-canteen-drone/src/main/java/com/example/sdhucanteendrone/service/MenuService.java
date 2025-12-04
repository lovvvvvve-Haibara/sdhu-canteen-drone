package com.example.sdhucanteendrone.service;

import com.example.sdhucanteendrone.dto.MenuDto;
import com.example.sdhucanteendrone.entity.enums.FoodShelfStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MenuService {

    // 分类
    MenuDto.CategoryList listCategories(Long canteenId);

    MenuDto.CategoryDetail createCategory(Long canteenId, MenuDto.CategoryCreateReq req);

    MenuDto.CategoryDetail updateCategory(Long canteenId, Long categoryId, MenuDto.CategoryUpdateReq req);

    void deleteCategory(Long canteenId, Long categoryId);

    // 菜品
    Page<MenuDto.FoodSummary> listFoods(
            Long canteenId,
            Long categoryId,
            FoodShelfStatus shelfStatus,
            String keyword,
            Pageable pageable
    );

    MenuDto.FoodDetail getFoodDetail(Long canteenId, Long foodId);

    MenuDto.FoodDetail createFood(Long canteenId, MenuDto.FoodCreateReq req);

    MenuDto.FoodDetail updateFood(Long canteenId, Long foodId, MenuDto.FoodUpdateReq req);

    void changeFoodShelfStatus(Long canteenId, Long foodId, FoodShelfStatus status);

    void updateFoodStock(Long canteenId, Long foodId, Integer stock);
}
