package com.preciazo.platform.iam.application.internal.commandservices;

import com.preciazo.platform.iam.application.commandservices.RoleCommandService;
import com.preciazo.platform.iam.domain.model.commands.SeedRolesCommand;
import com.preciazo.platform.iam.domain.model.entities.Role;
import com.preciazo.platform.iam.domain.model.valueobjects.Roles;
import com.preciazo.platform.iam.domain.repositories.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleCommandServiceImpl implements RoleCommandService {
    private final RoleRepository roles;

    public RoleCommandServiceImpl(RoleRepository roles) { this.roles = roles; }

    @Override @Transactional
    public void handle(SeedRolesCommand command) {
        for (Roles role : Roles.values()) {
            if (!roles.existsByName(role)) {
                roles.save(Role.create(role));
            }
        }
    }
}
