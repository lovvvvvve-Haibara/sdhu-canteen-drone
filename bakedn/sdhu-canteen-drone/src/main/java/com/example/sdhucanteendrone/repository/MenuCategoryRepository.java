package com.example.sdhucanteendrone.repository;

import com.example.sdhucanteendrone.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 菜单分类 menu_categories 对应的 Repository。
 */
@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

    /**
     * 查询指定食堂下的全部分类。
     */
    List<MenuCategory> findByCanteenId(Long canteenId);

    /**
     * 判断指定食堂下某个分类名是否存在。
     */
    boolean existsByCanteenIdAndName(Long canteenId, String name);

    /**
     * 查询指定食堂下指定名称的分类。
     */
    Optional<MenuCategory> findByCanteenIdAndName(Long canteenId, String name);
}
