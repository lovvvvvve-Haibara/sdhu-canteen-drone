package com.example.sdhucanteendrone.service.impl;

import com.example.sdhucanteendrone.Common.BizException;
import com.example.sdhucanteendrone.dto.OrderDto;
import com.example.sdhucanteendrone.entity.*;
import com.example.sdhucanteendrone.entity.enums.DeliverMethod;
import com.example.sdhucanteendrone.entity.enums.DroneStatus;
import com.example.sdhucanteendrone.entity.enums.OrderStatus;
import com.example.sdhucanteendrone.repository.*;
import com.example.sdhucanteendrone.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusEventRepository statusEventRepository;
    private final CanteenRepository canteenRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final DroneRepository droneRepository;

    // ============================================================
    // 创建订单
    // ============================================================

    @Override
    @Transactional
    public OrderDto.OrderDetail createOrder(OrderDto.OrderCreateReq req) {

        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw BizException.badRequest("购物车为空");
        }

        // 校验用户 / 食堂存在
        User customer = userRepository.findById(req.getCustomerId())
                .orElseThrow(() -> BizException.notFound("用户不存在"));

        Canteen canteen = canteenRepository.findById(req.getCanteenId())
                .orElseThrow(() -> BizException.notFound("食堂不存在"));

        // 创建订单主表（使用 Long 外键）
        Order order = new Order();
        order.setCustomerId(customer.getId());
        order.setCanteenId(canteen.getId());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliverMethod(req.getDeliveryMethod() == null
                ? DeliverMethod.DRONE
                : req.getDeliveryMethod());

        // 注意：你的实体叫 address（数据库是 delivery_address）
        order.setAddress(req.getDeliveryAddress());

        // 目前先不处理备注（你的实体里没有 remarks 字段）
        order.setAmountTotal(0); // 金额由触发器根据明细刷新
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());

        Order savedOrder = orderRepository.save(order);

        // 创建明细（使用 orderId / foodId / quantity）
        for (OrderDto.OrderItemCreateReq itemReq : req.getItems()) {
            Food food = foodRepository.findById(itemReq.getFoodId())
                    .orElseThrow(() -> BizException.badRequest("菜品不存在: " + itemReq.getFoodId()));


            OrderItem item = new OrderItem();
            item.setOrderId(savedOrder.getId());
            item.setFoodId(food.getId());
            item.setFoodName(food.getName());
            item.setUnitPrice(food.getPriceCent());
            item.setQuantity(itemReq.getQty());
            item.setCreatedAt(Instant.now());
            orderItemRepository.save(item);
        }

        // 记录初始状态事件
        createStatusEvent(savedOrder.getId(), OrderStatus.PENDING, "订单已创建");

        // 再查一遍，包含金额等字段
        return getOrderDetail(savedOrder.getId());
    }

    // ============================================================
    // 查询
    // ============================================================

    @Override
    public OrderDto.OrderDetail getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BizException.notFound("订单不存在"));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        List<OrderStatusEvent> events = statusEventRepository.findByOrderIdOrderByOccurredAtAsc(orderId);

        return toOrderDetail(order, items, events);
    }

    @Override
    public Page<OrderDto.OrderSummary> listCustomerOrders(Long customerId, OrderStatus status, Pageable pageable) {
        Page<Order> page;
        if (status != null) {
            page = orderRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status, pageable);
        } else {
            page = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        }
        return page.map(this::toOrderSummary);
    }

    @Override
    public Page<OrderDto.OrderSummary> listCanteenOrders(Long canteenId, OrderStatus status, Pageable pageable) {
        Page<Order> page;
        if (status != null) {
            page = orderRepository.findByCanteenIdAndStatusOrderByCreatedAtDesc(canteenId, status, pageable);
        } else {
            page = orderRepository.findByCanteenIdOrderByCreatedAtDesc(canteenId, pageable);
        }
        return page.map(this::toOrderSummary);
    }

    // ============================================================
    // 状态 / 取消
    // ============================================================

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BizException.notFound("订单不存在"));

        if (order.getStatus() == OrderStatus.CANCELED || order.getStatus() == OrderStatus.COMPLETED) {
            throw BizException.badRequest("该订单已完成或已取消");
        }

        order.setStatus(OrderStatus.CANCELED);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        createStatusEvent(orderId, OrderStatus.CANCELED,
                reason == null || reason.isBlank() ? "用户取消订单" : reason);
        // 库存回滚由数据库触发器负责
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BizException.notFound("订单不存在"));

        // 此处可以按业务增加状态流转校验
        order.setStatus(status);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        createStatusEvent(orderId, status, note);
    }

    @Override
    public OrderDto.OrderTimeline getOrderTimeline(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw BizException.notFound("订单不存在");
        }
        List<OrderStatusEvent> events = statusEventRepository.findByOrderIdOrderByOccurredAtAsc(orderId);
        OrderDto.OrderTimeline dto = new OrderDto.OrderTimeline();
        dto.setOrderId(orderId);
        dto.setEvents(events.stream().map(this::toStatusEventDto).collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional
    public void changeDeliveryMethod(Long orderId, DeliverMethod method) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BizException.notFound("订单不存在"));
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw BizException.badRequest("当前状态不允许修改配送方式");
        }
        order.setDeliverMethod(method);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
    }

    // ============================================================
    // 无人机相关
    // ============================================================

    @Override
    @Transactional
    public void assignDrone(Long orderId, Long droneId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BizException.notFound("订单不存在"));

        if (order.getDeliverMethod() != DeliverMethod.DRONE) {
            throw BizException.badRequest("该订单不是无人机配送");
        }

        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> BizException.notFound("无人机不存在"));

        if (drone.getStatus() != DroneStatus.IDLE) {
            throw BizException.badRequest("无人机当前不可用");
        }

        order.setDroneId(droneId);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void startDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BizException.notFound("订单不存在"));

        if (order.getDeliverMethod() != DeliverMethod.DRONE) {
            throw BizException.badRequest("该订单不是无人机配送");
        }

        Long droneId = order.getDroneId();
        if (droneId == null) {
            throw BizException.badRequest("订单尚未指派无人机");
        }

        Drone drone = droneRepository.findById(droneId)
                .orElseThrow(() -> BizException.notFound("无人机不存在"));

        // 修改订单状态
        order.setStatus(OrderStatus.SHIPPED);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
        createStatusEvent(orderId, OrderStatus.SHIPPED, "无人机已起飞");

        // 修改无人机状态
        drone.setStatus(DroneStatus.IN_MISSION);
        drone.setUpdatedAt(Instant.now());
        droneRepository.save(drone);
    }

    @Override
    @Transactional
    public void markDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> BizException.notFound("订单不存在"));

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw BizException.badRequest("当前状态不允许标记为已送达");
        }

        Long droneId = order.getDroneId();

        order.setStatus(OrderStatus.DELIVERED);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
        createStatusEvent(orderId, OrderStatus.DELIVERED, "订单已送达");

        if (droneId != null) {
            Drone drone = droneRepository.findById(droneId)
                    .orElseThrow(() -> BizException.notFound("无人机不存在"));
            drone.setStatus(DroneStatus.IDLE);
            drone.setUpdatedAt(Instant.now());
            droneRepository.save(drone);
        }
    }

    // ============================================================
    // 工具方法：状态事件
    // ============================================================

    private void createStatusEvent(Long orderId, OrderStatus status, String note) {
        OrderStatusEvent e = new OrderStatusEvent();
        e.setOrderId(orderId);
        e.setCode(status);
        e.setOccurredAt(Instant.now());
        e.setNote(note);
        e.setCreatedAt(Instant.now());
        statusEventRepository.save(e);
    }

    // ============================================================
    // 工具方法：DTO 映射
    // ============================================================

    private OrderDto.OrderSummary toOrderSummary(Order o) {
        OrderDto.OrderSummary dto = new OrderDto.OrderSummary();
        dto.setId(o.getId());
        dto.setCustomerId(o.getCustomerId());
        dto.setCanteenId(o.getCanteenId());
        dto.setStatus(o.getStatus());
        dto.setStatusLabel(o.getStatus().getLabel());
        dto.setDeliveryMethod(o.getDeliverMethod());
        dto.setAmountCent(o.getAmountTotal());
        dto.setDeliveryAddress(o.getAddress());
        ZoneId zone = ZoneId.systemDefault();
        dto.setCreatedAt(o.getCreatedAt() == null ? null :
                LocalDateTime.ofInstant(o.getCreatedAt(), zone));
        dto.setUpdatedAt(o.getUpdatedAt() == null ? null :
                LocalDateTime.ofInstant(o.getUpdatedAt(), zone));


        // 额外查用户 / 食堂名称（可做缓存优化）
        userRepository.findById(o.getCustomerId()).ifPresent(u -> {
            dto.setCustomerName(u.getDisplayName());
        });
        canteenRepository.findById(o.getCanteenId()).ifPresent(c -> {
            dto.setCanteenName(c.getName());
        });

        // 用明细填充一些冗余信息
        OrderItem first = orderItemRepository.findFirstByOrderIdOrderByIdAsc(o.getId());
        if (first != null) {
            dto.setFirstFoodName(first.getFoodName());
        }
        dto.setTotalItemCount(orderItemRepository.sumQtyByOrderId(o.getId()));

        return dto;
    }

    private OrderDto.OrderDetail toOrderDetail(Order o, List<OrderItem> items, List<OrderStatusEvent> events) {
        OrderDto.OrderDetail dto = new OrderDto.OrderDetail();
        dto.setId(o.getId());
        dto.setCustomerId(o.getCustomerId());
        dto.setCanteenId(o.getCanteenId());
        dto.setStatus(o.getStatus());
        dto.setStatusLabel(o.getStatus().getLabel());
        dto.setDeliveryMethod(o.getDeliverMethod());
        dto.setAmountCent(o.getAmountTotal());
        dto.setDeliveryAddress(o.getAddress());
        dto.setOtpCode(o.getOtp());
        dto.setCreatedAt(toLocalDateTime(o.getCreatedAt()));
        dto.setUpdatedAt(toLocalDateTime(o.getUpdatedAt()));

        // 查用户 / 食堂信息
        userRepository.findById(o.getCustomerId()).ifPresent(u -> {
            dto.setCustomerName(u.getDisplayName());
            dto.setCustomerPhone(u.getPhone());
        });
        canteenRepository.findById(o.getCanteenId()).ifPresent(c -> {
            dto.setCanteenName(c.getName());
        });

        // 无人机信息（如果有）
        if (o.getDroneId() != null) {
            dto.setDroneId(o.getDroneId());
            droneRepository.findById(o.getDroneId()).ifPresent(d -> {
                dto.setDroneCode(d.getCode());
            });
        }

        dto.setItems(items.stream().map(this::toOrderItemDto).collect(Collectors.toList()));
        dto.setTimeline(events.stream().map(this::toStatusEventDto).collect(Collectors.toList()));
        return dto;
    }

    private OrderDto.OrderItem toOrderItemDto(OrderItem item) {
        OrderDto.OrderItem dto = new OrderDto.OrderItem();
        dto.setId(item.getId());
        dto.setOrderId(item.getOrderId());
        dto.setFoodId(item.getFoodId());
        dto.setFoodName(item.getFoodName());
        dto.setUnitPriceCent(item.getUnitPrice());
        dto.setQty(item.getQuantity());
        // subtotal 在数据库是生成列，如果实体没有字段，可以不填
        dto.setCreatedAt(toLocalDateTime(item.getCreatedAt()));
        return dto;
    }

    private OrderDto.OrderStatusEvent toStatusEventDto(OrderStatusEvent e) {
        OrderDto.OrderStatusEvent dto = new OrderDto.OrderStatusEvent();
        dto.setId(e.getId());
        dto.setOrderId(e.getOrderId());
        dto.setCode(e.getCode());
        dto.setOccurredAt(toLocalDateTime(e.getOccurredAt()));
        dto.setNote(e.getNote());
        dto.setCreatedAt(toLocalDateTime(e.getCreatedAt()));
        return dto;
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null :
                LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
