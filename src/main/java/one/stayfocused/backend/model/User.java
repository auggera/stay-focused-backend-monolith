package one.stayfocused.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import one.stayfocused.backend.config.AvatarConfig;

import java.net.Authenticator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    private String password;

    @Column(nullable = false)
    private boolean emailVerified;

    private String provider;
    private String providerId;

    @Column(nullable = false)
    private String avatarUrl;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
