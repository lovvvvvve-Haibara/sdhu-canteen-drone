package com.example.sdhucanteendrone.controller;

import com.example.sdhucanteendrone.Common.Result;
import com.example.sdhucanteendrone.dto.OrderDto;
import com.example.sdhucanteendrone.entity.enums.DeliverMethod;
import com.example.sdhucanteendrone.entity.enums.OrderStatus;
import com.example.sdhucanteendrone.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 订单相关接口
 *
 * - 顾客：下单 / 查看订单 / 取消订单
 * - 食堂：查看订单 / 更新订单状态
 * - 配送：无人机指派、发货、送达（也可以拆到 DroneDeliveryController 中）
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ============================================================
    // 顾客侧：下单 & 查询自己的订单
    // ============================================================

    /**
     * 创建订单（顾客下单）
     */
    @PostMapping
    public Result<OrderDto.OrderDetail> createOrder(
            @Valid @RequestBody OrderDto.OrderCreateReq req) {
        OrderDto.OrderDetail detail = orderService.createOrder(req);
        return Result.success(detail);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<OrderDto.OrderDetail> getOrderById(@PathVariable("id") Long orderId) {
        return Result.success(orderService.getOrderDetail(orderId));
    }

    /**
     * 获取“当前用户”的订单列表
     * 目前先通过参数传 userId，之后可改为从登录态获取
     */
    @GetMapping("/self")
    public Result<Page<OrderDto.OrderSummary>> listMyOrders(
            @RequestParam("userId") Long userId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<OrderDto.OrderSummary> resultPage =
                orderService.listCustomerOrders(userId, status, PageRequest.of(page, size));
        return Result.success(resultPage);
    }

    /**
     * 顾客取消订单（如订单仍在 PENDING/CONFIRMED，可允许取消）
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(
            @PathVariable("id") Long orderId,
            @Valid @RequestBody OrderDto.OrderCancelReq req) {
        orderService.cancelOrder(orderId, req.getReason());
        return Result.success();
    }

    // ============================================================
    // 食堂侧：按食堂查看订单 & 更新状态
    // ============================================================

    /**
     * 食堂查看本食堂的订单列表（按状态过滤）
     */
    @GetMapping("/by-canteen/{canteenId}")
    public Result<Page<OrderDto.OrderSummary>> listCanteenOrders(
            @PathVariable("canteenId") Long canteenId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<OrderDto.OrderSummary> resultPage =
                orderService.listCanteenOrders(canteenId, status, PageRequest.of(page, size));
        return Result.success(resultPage);
    }

    /**
     * 更新订单状态（食堂操作：确认/打包/发货/完成/拒单等）
     */
    @PostMapping("/{id}/status")
    public Result<Void> updateOrderStatus(
            @PathVariable("id") Long orderId,
            @RequestParam("status") OrderStatus status,
            @RequestParam(value = "note", required = false) String note) {
        orderService.updateOrderStatus(orderId, status, note);
        return Result.success();
    }

    // ============================================================
    // 时间轴 / 事件
    // ============================================================

    /**
     * 获取订单状态时间轴（前端可用来画时间线）
     */
    @GetMapping("/{id}/timeline")
    public Result<OrderDto.OrderTimeline> getOrderTimeline(
            @PathVariable("id") Long orderId) {
        return Result.success(orderService.getOrderTimeline(orderId));
    }

    // ============================================================
    // 配送方式相关（可选）
    // ============================================================

    /**
     * 修改订单配送方式（DRONE / MANUAL）
     * 一般下单时确定，特殊情况下可调整
     */
    @PatchMapping("/{id}/delivery-method")
    public Result<Void> changeDeliveryMethod(
            @PathVariable("id") Long orderId,
            @RequestParam("method") DeliverMethod method) {
        orderService.changeDeliveryMethod(orderId, method);
        return Result.success();
    }

    // ============================================================
    // 无人机派送相关（可视为食堂/调度员操作）
    // ============================================================

    /**
     * 为订单指派无人机
     */
    @PostMapping("/{id}/assign-drone")
    public Result<Void> assignDrone(
            @PathVariable("id") Long orderId,
            @RequestParam("droneId") Long droneId) {
        orderService.assignDrone(orderId, droneId);
        return Result.success();
    }

    /**
     * 标记订单开始配送（无人机起飞 → SHIPPED）
     */
    @PostMapping("/{id}/start-delivery")
    public Result<Void> startDelivery(@PathVariable("id") Long orderId) {
        orderService.startDelivery(orderId);
        return Result.success();
    }

    /**
     * 标记订单已送达（无人机抵达 → DELIVERED）
     */
    @PostMapping("/{id}/mark-delivered")
    public Result<Void> markDelivered(@PathVariable("id") Long orderId) {
        orderService.markDelivered(orderId);
        return Result.success();
    }

}
