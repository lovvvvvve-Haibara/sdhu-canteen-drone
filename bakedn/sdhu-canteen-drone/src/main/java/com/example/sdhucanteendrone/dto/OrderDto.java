package com.example.sdhucanteendrone.dto;

import com.example.sdhucanteendrone.entity.enums.DeliverMethod;
import com.example.sdhucanteendrone.entity.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单相关 DTO
 *
 * 建议与 OrderController / OrderService 搭配使用。
 * - OrderSummary：列表展示
 * - OrderDetail：详情展示
 * - OrderItem*：订单明细项
 * - OrderCreateReq：顾客下单请求
 * - OrderCancelReq：顾客取消订单请求
 * - OrderTimeline / OrderStatusEvent：订单状态时间轴
 */
public class OrderDto {

    // ============================================================
    // 订单列表项（简要信息）
    // ============================================================

    @Data
    public static class OrderSummary {
        private Long id;

        private Long customerId;
        private String customerName;      // 方便前端展示（可选）

        private Long canteenId;
        private String canteenName;      // 方便前端展示（可选）

        private OrderStatus status;
        private String statusLabel;   // ★ 中文显示给前端用
        private DeliverMethod deliveryMethod;

        /**
         * 总金额（分），对应 orders.amount_total
         */
        private Integer amountCent;

        /**
         * 配送地址，对应 orders.delivery_address
         */
        private String deliveryAddress;

        /**
         * 列表页常见的冗余字段
         */
        private String firstFoodName;     // 第一个菜品名称（比如“宫保鸡丁盖饭”）
        private Integer totalItemCount;   // 菜品总份数（sum(qty)）

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    // ============================================================
    // 订单详情
    // ============================================================

    @Data
    public static class OrderDetail {
        private Long id;

        private Long customerId;
        private String customerName;
        private String customerPhone;

        private Long canteenId;
        private String canteenName;

        private OrderStatus status;
        private String statusLabel;   // ★ 中文显示给前端用
        private DeliverMethod deliveryMethod;

        private Long droneId;            // 对应 orders.drone_id
        private String droneCode;        // 可选：无人机编号，方便展示

        private Integer amountCent;      // 总金额（分）
        private String remarks;          // 备注
        private String deliveryAddress;  // 配送地址
        private String otpCode;          // 取餐码（若有）

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        /**
         * 订单明细列表
         */
        private List<OrderItem> items;

        /**
         * 简要时间轴（可选，若不需要，可以删掉，在单独的接口返回 OrderTimeline）
         */
        private List<OrderStatusEvent> timeline;
    }

    // ============================================================
    // 订单明细项（对标 order_items）
    // ============================================================

    @Data
    public static class OrderItem {
        private Long id;
        private Long orderId;
        private Long foodId;

        /**
         * 下单时的菜名快照
         */
        private String foodName;

        /**
         * 下单时的单价（分），对应 order_items.unit_price
         */
        private Integer unitPriceCent;

        /**
         * 数量，对应 order_items.qty
         */
        private Integer qty;

        /**
         * 小计金额（分），对应 order_items.subtotal
         */
        private Integer subtotalCent;

        private LocalDateTime createdAt;
    }

    // ============================================================
    // 下单请求（顾客创建订单）
    // ============================================================

    @Data
    public static class OrderCreateReq {
        /**
         * 当前登录用户 ID（临时从参数传，之后可从登录态获取）
         */
        private Long customerId;

        /**
         * 下单食堂
         */
        private Long canteenId;

        /**
         * 配送方式：DRONE / MANUAL
         */
        private DeliverMethod deliveryMethod;

        /**
         * 配送地址（宿舍/楼栋-房间号）
         */
        private String deliveryAddress;

        /**
         * 用户备注，例如“不吃香菜”
         */
        private String remarks;

        /**
         * 购物车明细
         */
        private List<OrderItemCreateReq> items;
    }

    /**
     * 下单时的每一条明细
     */
    @Data
    public static class OrderItemCreateReq {
        private Long foodId;
        private Integer qty;
    }

    // ============================================================
    // 顾客取消订单
    // ============================================================

    @Data
    public static class OrderCancelReq {
        /**
         * 取消原因（可选）
         */
        private String reason;
    }

    // ============================================================
    // 订单时间轴（状态流转）
    // ============================================================

    @Data
    public static class OrderTimeline {
        private Long orderId;
        private List<OrderStatusEvent> events;
    }

    /**
     * 单条状态事件，对应表 order_status_events：
     *  - code        ENUM('PENDING','CONFIRMED','PACKED','SHIPPED','DELIVERED','COMPLETED','CANCELED')
     *  - occurred_at DATETIME
     *  - note        备注
     */
    @Data
    public static class OrderStatusEvent {
        private Long id;
        private Long orderId;

        private OrderStatus code;
        private LocalDateTime occurredAt;
        private String note;

        private LocalDateTime createdAt;
    }
}
