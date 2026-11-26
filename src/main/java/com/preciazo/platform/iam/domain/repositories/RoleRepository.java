package com.preciazo.platform.iam.domain.repositories;

import com.preciazo.platform.iam.domain.model.entities.Role;
import com.preciazo.platform.iam.domain.model.valueobjects.Roles;
import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByName(Roles name);
    boolean existsByName(Roles name);
    List<Role> findAll();
    Role save(Role role);
}
