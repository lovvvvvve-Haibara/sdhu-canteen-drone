package com.example.sdhucanteendrone.service.impl;

import com.example.sdhucanteendrone.Common.BizException;
import com.example.sdhucanteendrone.dto.MenuDto;
import com.example.sdhucanteendrone.entity.Canteen;
import com.example.sdhucanteendrone.entity.Food;
import com.example.sdhucanteendrone.entity.MenuCategory;
import com.example.sdhucanteendrone.entity.enums.FoodShelfStatus;
import com.example.sdhucanteendrone.repository.CanteenRepository;
import com.example.sdhucanteendrone.repository.FoodRepository;
import com.example.sdhucanteendrone.repository.MenuCategoryRepository;
import com.example.sdhucanteendrone.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuServiceImpl implements MenuService {

    private final CanteenRepository canteenRepository;
    private final MenuCategoryRepository categoryRepository;
    private final FoodRepository foodRepository;

    // ============================================================
    // 分类
    // ============================================================

    @Override
    public MenuDto.CategoryList listCategories(Long canteenId) {
        // 校验食堂存在（可选）
        canteenRepository.findById(canteenId)
                .orElseThrow(() -> BizException.notFound("食堂不存在"));

        List<MenuCategory> list = categoryRepository.findByCanteenId(canteenId);
        list.sort(Comparator.comparing(MenuCategory::getSort));

        MenuDto.CategoryList dto = new MenuDto.CategoryList();
        dto.setCanteenId(canteenId);
        dto.setCategories(
                list.stream().map(this::toCategorySummary).collect(Collectors.toList())
        );
        return dto;
    }

    @Override
    @Transactional
    public MenuDto.CategoryDetail createCategory(Long canteenId, MenuDto.CategoryCreateReq req) {
        // 校验食堂存在
        Canteen canteen = canteenRepository.findById(canteenId)
                .orElseThrow(() -> BizException.notFound("食堂不存在"));

        // 名称在同一食堂下唯一
        if (categoryRepository.existsByCanteenIdAndName(canteenId, req.getName())) {
            throw BizException.badRequest("该分类名称已存在");
        }

        MenuCategory cat = new MenuCategory();
        cat.setCanteenId(canteen.getId());
        cat.setName(req.getName());
        cat.setSort(req.getSort() == null ? 0 : req.getSort());
        cat.setCreatedAt(Instant.now());
        cat.setUpdatedAt(Instant.now());

        return toCategoryDetail(categoryRepository.save(cat));
    }

    @Override
    @Transactional
    public MenuDto.CategoryDetail updateCategory(Long canteenId, Long categoryId, MenuDto.CategoryUpdateReq req) {
        MenuCategory cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> BizException.notFound("分类不存在"));

        if (!Objects.equals(cat.getCanteenId(), canteenId)) {
            throw BizException.badRequest("分类不属于该食堂");
        }

        if (req.getName() != null && !req.getName().isBlank()
                && !req.getName().equals(cat.getName())) {
            categoryRepository.findByCanteenIdAndName(canteenId, req.getName())
                    .filter(other -> !other.getId().equals(categoryId))
                    .ifPresent(other -> {
                        throw BizException.badRequest("该分类名称已存在");
                    });
            cat.setName(req.getName());
        }
        if (req.getSort() != null) {
            cat.setSort(req.getSort());
        }

        cat.setUpdatedAt(Instant.now());
        return toCategoryDetail(categoryRepository.save(cat));
    }

    @Override
    @Transactional
    public void deleteCategory(Long canteenId, Long categoryId) {
        MenuCategory cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> BizException.notFound("分类不存在"));

        if (!Objects.equals(cat.getCanteenId(), canteenId)) {
            throw BizException.badRequest("分类不属于该食堂");
        }

        long count = foodRepository.countByCategoryId(categoryId);
        if (count > 0) {
            throw BizException.badRequest("该分类下仍有菜品，无法删除");
        }

        categoryRepository.delete(cat);
    }

    // ============================================================
    // 菜品
    // ============================================================

    @Override
    public Page<MenuDto.FoodSummary> listFoods(
            Long canteenId,
            Long categoryId,
            FoodShelfStatus shelfStatus,
            String keyword,
            Pageable pageable
    ) {
        Boolean onShelf = null;
        if (shelfStatus != null) {
            onShelf = (shelfStatus == FoodShelfStatus.ON);
        }

        Page<Food> page = foodRepository.searchFoods(
                canteenId,
                categoryId,
                onShelf,
                keyword == null ? "" : keyword.trim(),
                pageable
        );
        return page.map(this::toFoodSummary);
    }

    @Override
    public MenuDto.FoodDetail getFoodDetail(Long canteenId, Long foodId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> BizException.notFound("菜品不存在"));
        if (!Objects.equals(food.getCanteenId(), canteenId)) {
            throw BizException.badRequest("菜品不属于该食堂");
        }
        return toFoodDetail(food);
    }

    @Override
    @Transactional
    public MenuDto.FoodDetail createFood(Long canteenId, MenuDto.FoodCreateReq req) {
        // 校验食堂存在
        canteenRepository.findById(canteenId)
                .orElseThrow(() -> BizException.notFound("食堂不存在"));

        MenuCategory category = null;
        if (req.getCategoryId() != null) {
            category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> BizException.notFound("分类不存在"));
            if (!Objects.equals(category.getCanteenId(), canteenId)) {
                throw BizException.badRequest("分类不属于该食堂");
            }
        }

        // 名称在同一食堂下唯一
        if (foodRepository.existsByCanteenIdAndName(canteenId, req.getName())) {
            throw BizException.badRequest("该菜品名称已存在");
        }

        Food food = new Food();
        food.setCanteenId(canteenId);
        food.setCategoryId(category == null ? null : category.getId());
        food.setName(req.getName());
        food.setPriceCent(req.getPriceCent());
        food.setStock(req.getStock() == null ? 0 : req.getStock());
        food.setImageUrl(req.getImageUrl());
        food.setOnShelf(req.getOnShelf() == null || req.getOnShelf()); // 默认上架
        food.setCreatedAt(Instant.now());
        food.setUpdatedAt(Instant.now());

        return toFoodDetail(foodRepository.save(food));
    }

    @Override
    @Transactional
    public MenuDto.FoodDetail updateFood(Long canteenId, Long foodId, MenuDto.FoodUpdateReq req) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> BizException.notFound("菜品不存在"));

        if (!Objects.equals(food.getCanteenId(), canteenId)) {
            throw BizException.badRequest("菜品不属于该食堂");
        }

        if (req.getCategoryId() != null) {
            MenuCategory cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> BizException.notFound("分类不存在"));
            if (!Objects.equals(cat.getCanteenId(), canteenId)) {
                throw BizException.badRequest("分类不属于该食堂");
            }
            food.setCategoryId(cat.getId());
        }

        if (req.getName() != null && !req.getName().isBlank()
                && !req.getName().equals(food.getName())) {
            foodRepository.findByCanteenIdAndName(canteenId, req.getName())
                    .filter(other -> !other.getId().equals(foodId))
                    .ifPresent(other -> {
                        throw BizException.badRequest("该菜品名称已存在");
                    });
            food.setName(req.getName());
        }

        if (req.getPriceCent() != null) {
            food.setPriceCent(req.getPriceCent());
        }
        if (req.getStock() != null) {
            food.setStock(req.getStock());
        }
        if (req.getImageUrl() != null) {
            food.setImageUrl(req.getImageUrl());
        }
        if (req.getOnShelf() != null) {
            food.setOnShelf(req.getOnShelf());
        }

        food.setUpdatedAt(Instant.now());
        return toFoodDetail(foodRepository.save(food));
    }

    @Override
    @Transactional
    public void changeFoodShelfStatus(Long canteenId, Long foodId, FoodShelfStatus status) {
        Boolean onShelf = (status == FoodShelfStatus.ON);
        int updated = foodRepository.updateOnShelfByCanteenIdAndId(canteenId, foodId, onShelf);
        if (updated == 0) {
            throw BizException.notFound("菜品不存在");
        }
    }

    @Override
    @Transactional
    public void updateFoodStock(Long canteenId, Long foodId, Integer stock) {
        if (stock == null || stock < 0) {
            throw BizException.badRequest("库存必须为非负整数");
        }
        int updated = foodRepository.updateStockByCanteenIdAndId(canteenId, foodId, stock);
        if (updated == 0) {
            throw BizException.notFound("菜品不存在");
        }
    }

    // ============================================================
    // 工具方法
    // ============================================================

    private MenuDto.CategorySummary toCategorySummary(MenuCategory cat) {
        MenuDto.CategorySummary dto = new MenuDto.CategorySummary();
        dto.setId(cat.getId());
        dto.setCanteenId(cat.getCanteenId());
        dto.setName(cat.getName());
        dto.setSort(cat.getSort());
        return dto;
    }

    private MenuDto.CategoryDetail toCategoryDetail(MenuCategory cat) {
        MenuDto.CategoryDetail dto = new MenuDto.CategoryDetail();
        dto.setId(cat.getId());
        dto.setCanteenId(cat.getCanteenId());
        dto.setName(cat.getName());
        dto.setSort(cat.getSort());
        dto.setCreatedAt(toLocalDateTime(cat.getCreatedAt()));
        dto.setUpdatedAt(toLocalDateTime(cat.getUpdatedAt()));
        return dto;
    }

    private MenuDto.FoodSummary toFoodSummary(Food food) {
        MenuDto.FoodSummary dto = new MenuDto.FoodSummary();
        dto.setId(food.getId());
        dto.setCanteenId(food.getCanteenId());
        dto.setCategoryId(food.getCategoryId());
        // categoryName 需要单独查，可选：这里先不查，前端如需显示可加缓存/批量查询
        dto.setName(food.getName());
        dto.setPriceCent(food.getPriceCent());
        dto.setStock(food.getStock());
        dto.setOnShelf(food.getOnShelf());
        dto.setImageUrl(food.getImageUrl());
        return dto;
    }

    private MenuDto.FoodDetail toFoodDetail(Food food) {
        MenuDto.FoodDetail dto = new MenuDto.FoodDetail();
        dto.setId(food.getId());
        dto.setCanteenId(food.getCanteenId());
        dto.setCategoryId(food.getCategoryId());
        dto.setName(food.getName());
        dto.setPriceCent(food.getPriceCent());
        dto.setStock(food.getStock());
        dto.setOnShelf(food.getOnShelf());
        dto.setImageUrl(food.getImageUrl());
        dto.setCreatedAt(toLocalDateTime(food.getCreatedAt()));
        dto.setUpdatedAt(toLocalDateTime(food.getUpdatedAt()));
        return dto;
    }
    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null :
                LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
