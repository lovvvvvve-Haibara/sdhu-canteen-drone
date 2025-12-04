package com.example.sdhucanteendrone.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "foods")
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "canteen_id", nullable = false)
    private Long canteenId;

    @Column(name = "price_cent", nullable = false)
    private Integer priceCent;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "on_shelf", nullable = false)
    private Boolean onShelf;

    @Column(length = 500)
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
