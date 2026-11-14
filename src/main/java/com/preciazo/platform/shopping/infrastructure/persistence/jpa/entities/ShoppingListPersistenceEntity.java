package com.preciazo.platform.shopping.infrastructure.persistence.jpa.entities;

import com.preciazo.platform.shopping.domain.model.valueobjects.ListStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "shopping_lists", indexes = @Index(name = "idx_shopping_list_owner_created", columnList = "owner_id,created_at,id"))
public class ShoppingListPersistenceEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "owner_id", nullable = false, updatable = false) private long ownerId;
    @Column(nullable = false, length = 255) private String name;
    @Column(nullable = false, length = 3) private String currency;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ListStatus status;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Version @Column(nullable = false) private Long version;

    protected ShoppingListPersistenceEntity() { }
    public ShoppingListPersistenceEntity(Long id, long ownerId, String name, String currency, ListStatus status, Instant createdAt, Long version) {
        this.id = id; this.ownerId = ownerId; this.name = name; this.currency = currency; this.status = status; this.createdAt = createdAt; this.version = version;
    }
    public Long getId() { return id; }
    public long getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getCurrency() { return currency; }
    public ListStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Long getVersion() { return version; }
}
