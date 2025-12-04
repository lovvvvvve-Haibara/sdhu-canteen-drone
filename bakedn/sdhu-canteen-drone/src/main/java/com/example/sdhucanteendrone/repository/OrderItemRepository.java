package com.example.sdhucanteendrone.repository;

import com.example.sdhucanteendrone.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单明细 order_items 对应的 Repository。
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 查询某个订单的所有明细。
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * 查询某个订单的第一条明细（用于列表页展示第一个菜品名）。
     */
    OrderItem findFirstByOrderIdOrderByIdAsc(Long orderId);

    /**
     * 统计某个订单的总份数（sum(qty)）。
     */
    @Query("select coalesce(sum(oi.quantity), 0) from OrderItem oi where oi.orderId = :orderId")
    int sumQtyByOrderId(@Param("orderId") Long orderId);
}
