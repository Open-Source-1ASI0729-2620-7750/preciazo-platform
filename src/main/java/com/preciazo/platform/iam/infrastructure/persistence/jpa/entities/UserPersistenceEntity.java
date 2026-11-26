package com.preciazo.platform.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "iam_users")
public class UserPersistenceEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 160) private String email;
    @Column(name = "full_name", nullable = false, length = 160) private String fullName;
    @Column(name = "password_hash", nullable = false, length = 255) private String passwordHash;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "iam_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RolePersistenceEntity> roles = new HashSet<>();

    protected UserPersistenceEntity() { }
    public UserPersistenceEntity(Long id, String email, String fullName, String passwordHash, Instant createdAt, Set<RolePersistenceEntity> roles) {
        this.id = id; this.email = email; this.fullName = fullName; this.passwordHash = passwordHash;
        this.createdAt = createdAt; this.roles = roles;
    }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPasswordHash() { return passwordHash; }
    public Instant getCreatedAt() { return createdAt; }
    public Set<RolePersistenceEntity> getRoles() { return roles; }
}
