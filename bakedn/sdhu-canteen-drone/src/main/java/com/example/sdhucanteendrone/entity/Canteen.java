package com.example.sdhucanteendrone.entity;

import com.example.sdhucanteendrone.entity.enums.CanteenOpenStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "canteens")
public class Canteen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 对应 SQL: name VARCHAR(150) NOT NULL
    @Column(nullable = false, length = 150)
    private String name;

    // 对应 SQL: location VARCHAR(255) NOT NULL
    @Column(nullable = false, length = 255)
    private String location;

    // 对应 SQL: open_status ENUM('OPEN','CLOSED') NOT NULL
    @Enumerated(EnumType.STRING)
    @Column(name = "open_status", nullable = false, length = 20)
    private CanteenOpenStatus openStatus;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
