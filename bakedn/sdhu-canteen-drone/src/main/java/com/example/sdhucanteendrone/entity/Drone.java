package com.example.sdhucanteendrone.entity;

import com.example.sdhucanteendrone.entity.enums.DroneStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "drones")
public class Drone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "max_payload_kg", nullable = false)
    private Double maxPayloadKg;

    // ✅ 新增：battery 字段，和表结构对齐
    @Column(nullable = false)
    private Integer battery;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DroneStatus status;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "location" , length = 255)
    private String location;
}
