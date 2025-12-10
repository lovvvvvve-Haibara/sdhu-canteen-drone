package com.example.sdhucanteendrone.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "food_id", nullable = false)
    private Long foodId;

    @Column(name = "food_name", nullable = false, length = 200)
    private String foodName;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "qty", nullable = false)
    private Integer quantity;

    @Column(name = "subtotal", insertable = false, updatable = false)
    private Integer subtotal;   // 若使用数据库生成列可保留

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
