package com.example.sdhucanteendrone.repository;

import com.example.sdhucanteendrone.entity.Canteen;
import com.example.sdhucanteendrone.entity.enums.CanteenOpenStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 食堂实体对应的 Repository，用于对 canteens 表进行增删改查。
 */
@Repository
public interface CanteenRepository extends JpaRepository<Canteen, Long> {

    /**
     * 根据名称查询食堂（名称唯一）。
     */
    Optional<Canteen> findByName(String name);

    /**
     * 判断指定名称的食堂是否存在。
     */
    boolean existsByName(String name);

    /**
     * 按营业状态分页查询食堂列表。
     */
    Page<Canteen> findByOpenStatus(CanteenOpenStatus openStatus, Pageable pageable);

    /**
     * 更新食堂营业状态。
     */
    @Modifying
    @Query("update Canteen c set c.openStatus = :status where c.id = :id")
    int updateOpenStatusById(@Param("id") Long id,
                             @Param("status") CanteenOpenStatus status);
}
