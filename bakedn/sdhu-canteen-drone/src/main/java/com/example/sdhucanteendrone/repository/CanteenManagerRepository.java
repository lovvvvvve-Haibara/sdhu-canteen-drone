package com.example.sdhucanteendrone.repository;

import com.example.sdhucanteendrone.entity.CanteenManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 食堂管理员关联表（canteen_managers）对应的 Repository。
 */
@Repository
public interface CanteenManagerRepository extends JpaRepository<CanteenManager, Long> {

    /**
     * 根据食堂 ID 查询该食堂的管理员列表。
     */
    List<CanteenManager> findByCanteenId(Long canteenId);

    /**
     * 判断某个用户是否已经是该食堂的管理员。
     */
    boolean existsByCanteenIdAndUserId(Long canteenId, Long userId);

    /**
     * 删除某个食堂的指定管理员关系。
     *
     * @return 删除的行数
     */
    int deleteByCanteenIdAndUserId(Long canteenId, Long userId);
}
