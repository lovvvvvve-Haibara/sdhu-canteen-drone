package com.example.sdhucanteendrone.entity;

import com.example.sdhucanteendrone.entity.enums.DeliverMethod;
import com.example.sdhucanteendrone.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "canteen_id", nullable = false)
    private Long canteenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "deliver_method", nullable = false, length = 20)
    private DeliverMethod deliverMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "amount_total", nullable = false)
    private Integer amountTotal;

    // 对应 SQL: remarks VARCHAR(255)
    @Column(name = "remarks")
    private String remarks;

    // 对应 SQL: delivery_address VARCHAR(255) NOT NULL
    @Column(name = "delivery_address", nullable = false, length = 255)
    private String address;

    // 对应 SQL: otp_code VARCHAR(10)
    @Column(name = "otp_code", length = 10)
    private String otp;

    @Column(name = "drone_id")
    private Long droneId;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
