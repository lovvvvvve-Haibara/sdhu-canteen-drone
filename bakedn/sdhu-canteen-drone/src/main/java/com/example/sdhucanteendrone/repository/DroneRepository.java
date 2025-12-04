package com.example.sdhucanteendrone.repository;

import com.example.sdhucanteendrone.entity.Drone;
import com.example.sdhucanteendrone.entity.enums.DroneStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 无人机 drones 表对应的 Repository。
 */
@Repository
public interface DroneRepository extends JpaRepository<Drone, Long> {

    /**
     * 按状态分页查询无人机。
     */
    Page<Drone> findByStatus(DroneStatus status, Pageable pageable);

    /**
     * 判断无人机编号是否已存在。
     */
    boolean existsByCode(String code);

    /**
     * 更新无人机状态。
     */
    @Modifying
    @Query("update Drone d set d.status = :status where d.id = :id")
    int updateStatusById(@Param("id") Long id,
                         @Param("status") DroneStatus status);
}
