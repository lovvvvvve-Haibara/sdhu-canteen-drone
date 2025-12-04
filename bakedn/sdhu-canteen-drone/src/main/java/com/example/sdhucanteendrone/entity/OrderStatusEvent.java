package com.example.sdhucanteendrone.entity;

import com.example.sdhucanteendrone.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "order_status_events")
public class OrderStatusEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    // 对应表字段 code ENUM(...)
    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, length = 20)
    private OrderStatus code;

    // 对应 occurred_at DATETIME
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    // 对应 note VARCHAR(255)
    @Column(name = "note")
    private String note;

    // 对应 created_at TIMESTAMP
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
