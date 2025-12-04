package com.example.sdhucanteendrone.repository;

import com.example.sdhucanteendrone.entity.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 菜品 foods 表对应的 Repository。
 */
@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    /**
     * 统计某个分类下的菜品数量。
     */
    long countByCategoryId(Long categoryId);

    /**
     * 判断某个食堂下，同名菜品是否存在。
     */
    boolean existsByCanteenIdAndName(Long canteenId, String name);

    /**
     * 查询某个食堂下指定名称的菜品。
     */
    Optional<Food> findByCanteenIdAndName(Long canteenId, String name);

    /**
     * 复杂搜索：按食堂 / 分类 / 上下架状态 / 关键字 查询菜品并分页。
     *
     * 说明：
     * - canteenId 必填
     * - categoryId / onShelf 可为 null 表示不按该条件过滤
     * - keyword 为空字符串表示不过滤
     */
    @Query("""
           select f from Food f
           where f.canteenId = :canteenId
             and (:categoryId is null or f.categoryId = :categoryId)
             and (:onShelf is null or f.onShelf = :onShelf)
             and (:keyword = '' or lower(f.name) like lower(concat('%', :keyword, '%')))
           """)
    Page<Food> searchFoods(@Param("canteenId") Long canteenId,
                           @Param("categoryId") Long categoryId,
                           @Param("onShelf") Boolean onShelf,
                           @Param("keyword") String keyword,
                           Pageable pageable);

    /**
     * 更新菜品的上架状态。
     */
    @Modifying
    @Query("update Food f set f.onShelf = :onShelf where f.canteenId = :canteenId and f.id = :foodId")
    int updateOnShelfByCanteenIdAndId(@Param("canteenId") Long canteenId,
                                      @Param("foodId") Long foodId,
                                      @Param("onShelf") Boolean onShelf);

    /**
     * 更新菜品库存。
     */
    @Modifying
    @Query("update Food f set f.stock = :stock where f.canteenId = :canteenId and f.id = :foodId")
    int updateStockByCanteenIdAndId(@Param("canteenId") Long canteenId,
                                    @Param("foodId") Long foodId,
                                    @Param("stock") Integer stock);
}
