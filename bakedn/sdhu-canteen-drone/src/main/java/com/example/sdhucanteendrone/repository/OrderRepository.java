package com.example.sdhucanteendrone.repository;

import com.example.sdhucanteendrone.entity.Order;
import com.example.sdhucanteendrone.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 订单主表 orders 对应的 Repository。
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ========== 顾客维度 ==========

    /**
     * 按顾客分页查询订单（按创建时间倒序）。
     */
    Page<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    /**
     * 按顾客 + 状态分页查询订单。
     */
    Page<Order> findByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId,
                                                              OrderStatus status,
                                                              Pageable pageable);

    // ========== 食堂维度 ==========

    /**
     * 按食堂分页查询订单。
     */
    Page<Order> findByCanteenIdOrderByCreatedAtDesc(Long canteenId, Pageable pageable);

    /**
     * 按食堂 + 状态分页查询订单。
     */
    Page<Order> findByCanteenIdAndStatusOrderByCreatedAtDesc(Long canteenId,
                                                             OrderStatus status,
                                                             Pageable pageable);
}
