package com.example.sdhucanteendrone.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "canteen_managers")
public class CanteenManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "canteen_id", nullable = false)
    private Long canteenId;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
