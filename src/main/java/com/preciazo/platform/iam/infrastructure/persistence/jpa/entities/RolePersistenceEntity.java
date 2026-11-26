package com.preciazo.platform.iam.infrastructure.persistence.jpa.entities;

import com.preciazo.platform.iam.domain.model.valueobjects.Roles;
import jakarta.persistence.*;

@Entity
@Table(name = "iam_roles")
public class RolePersistenceEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, unique = true, length = 32) private Roles name;

    protected RolePersistenceEntity() { }
    public RolePersistenceEntity(Long id, Roles name) { this.id = id; this.name = name; }
    public Long getId() { return id; }
    public Roles getName() { return name; }
}
