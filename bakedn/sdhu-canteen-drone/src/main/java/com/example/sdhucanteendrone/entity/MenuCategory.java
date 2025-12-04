package com.example.sdhucanteendrone.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "menu_categories")
public class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "canteen_id", nullable = false)
    private Long canteenId;

    // 分类顺序（你的 MenuServiceImpl 中用到了 getSort/setSort）
    @Column(nullable = false)
    private Integer sort = 0;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
