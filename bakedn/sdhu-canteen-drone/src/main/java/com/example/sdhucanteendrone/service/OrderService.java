package com.example.sdhucanteendrone.service;

import com.example.sdhucanteendrone.dto.OrderDto;
import com.example.sdhucanteendrone.entity.enums.DeliverMethod;
import com.example.sdhucanteendrone.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderDto.OrderDetail createOrder(OrderDto.OrderCreateReq req);

    OrderDto.OrderDetail getOrderDetail(Long orderId);

    Page<OrderDto.OrderSummary> listCustomerOrders(Long customerId, OrderStatus status, Pageable pageable);

    Page<OrderDto.OrderSummary> listCanteenOrders(Long canteenId, OrderStatus status, Pageable pageable);

    void cancelOrder(Long orderId, String reason);

    void updateOrderStatus(Long orderId, OrderStatus status, String note);

    OrderDto.OrderTimeline getOrderTimeline(Long orderId);

    void changeDeliveryMethod(Long orderId, DeliverMethod method);

    void assignDrone(Long orderId, Long droneId);

    void startDelivery(Long orderId);

    void markDelivered(Long orderId);
}
