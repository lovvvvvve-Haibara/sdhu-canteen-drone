package com.example.sdhucanteendrone.repository;

import com.example.sdhucanteendrone.entity.OrderStatusEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单状态事件 order_status_events 对应的 Repository。
 */
@Repository
public interface OrderStatusEventRepository extends JpaRepository<OrderStatusEvent, Long> {

    /**
     * 按时间升序查询某个订单的状态事件，用于时间轴展示。
     */
    List<OrderStatusEvent> findByOrderIdOrderByOccurredAtAsc(Long orderId);
}
