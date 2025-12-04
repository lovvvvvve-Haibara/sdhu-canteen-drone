package com.example.sdhucanteendrone.entity;

import com.example.sdhucanteendrone.entity.enums.UserRole;
import com.example.sdhucanteendrone.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username; // 学号或工号

    @Column(name = "username_lc", nullable = false, unique = true, length = 64)
    private String usernameLc;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role; // CUSTOMER, CANTEEN, ADMIN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status; // ACTIVE, LOCKED, DISABLED

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(length = 20)
    private String phone;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    private void fillUsernameLc() {
        if (username != null) {
            this.usernameLc = username.toLowerCase();
        }
    }
}
