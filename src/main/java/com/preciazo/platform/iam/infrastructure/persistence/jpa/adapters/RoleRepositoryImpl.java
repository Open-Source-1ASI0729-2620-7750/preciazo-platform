package com.preciazo.platform.iam.infrastructure.persistence.jpa.adapters;

import com.preciazo.platform.iam.domain.model.entities.Role;
import com.preciazo.platform.iam.domain.model.valueobjects.Roles;
import com.preciazo.platform.iam.domain.repositories.RoleRepository;
import com.preciazo.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import com.preciazo.platform.iam.infrastructure.persistence.jpa.repositories.RolePersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepositoryImpl implements RoleRepository {
    private final RolePersistenceRepository jpa;
    public RoleRepositoryImpl(RolePersistenceRepository jpa) { this.jpa = jpa; }

    @Override public Optional<Role> findByName(Roles name) {
        return jpa.findByName(name).map(e -> Role.restore(e.getId(), e.getName()));
    }
    @Override public boolean existsByName(Roles name) { return jpa.existsByName(name); }
    @Override public List<Role> findAll() {
        return jpa.findAll().stream().map(e -> Role.restore(e.getId(), e.getName())).toList();
    }
    @Override public Role save(Role role) {
        var saved = jpa.saveAndFlush(new RolePersistenceEntity(role.getId(), role.getName()));
        return Role.restore(saved.getId(), saved.getName());
    }
}
