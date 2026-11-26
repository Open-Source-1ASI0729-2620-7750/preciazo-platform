package com.preciazo.platform.iam.infrastructure.persistence.jpa.adapters;

import com.preciazo.platform.iam.domain.model.aggregates.User;
import com.preciazo.platform.iam.domain.model.entities.Role;
import com.preciazo.platform.iam.domain.repositories.UserRepository;
import com.preciazo.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import com.preciazo.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.preciazo.platform.iam.infrastructure.persistence.jpa.repositories.RolePersistenceRepository;
import com.preciazo.platform.iam.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final UserPersistenceRepository jpa;
    private final RolePersistenceRepository roleJpa;

    public UserRepositoryImpl(UserPersistenceRepository jpa, RolePersistenceRepository roleJpa) {
        this.jpa = jpa;
        this.roleJpa = roleJpa;
    }

    @Override public Optional<User> findById(long id) { return jpa.findById(id).map(this::toDomain); }
    @Override public Optional<User> findByEmail(String email) { return jpa.findByEmail(email).map(this::toDomain); }
    @Override public boolean existsByEmail(String email) { return jpa.existsByEmail(email); }
    @Override public List<User> findAll() { return jpa.findAll().stream().map(this::toDomain).toList(); }

    @Override public User save(User user) {
        Set<RolePersistenceEntity> roleEntities = user.getRoles().stream()
                .map(r -> roleJpa.findByName(r.getName())
                        .orElseGet(() -> roleJpa.saveAndFlush(new RolePersistenceEntity(null, r.getName()))))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var entity = new UserPersistenceEntity(user.getId(), user.getEmail(), user.getFullName(),
                user.getPasswordHash(), user.getCreatedAt(), roleEntities);
        return toDomain(jpa.saveAndFlush(entity));
    }

    private User toDomain(UserPersistenceEntity e) {
        Set<Role> domainRoles = e.getRoles().stream()
                .map(r -> Role.restore(r.getId(), r.getName()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return User.restore(e.getId(), e.getEmail(), e.getFullName(), e.getPasswordHash(), domainRoles, e.getCreatedAt());
    }
}
