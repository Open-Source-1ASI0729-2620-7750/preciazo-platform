package com.preciazo.platform.iam.application.internal.queryservices;

import com.preciazo.platform.iam.application.queryservices.RoleQueryService;
import com.preciazo.platform.iam.domain.model.entities.Role;
import com.preciazo.platform.iam.domain.model.queries.IamQueries;
import com.preciazo.platform.iam.domain.repositories.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class RoleQueryServiceImpl implements RoleQueryService {
    private final RoleRepository roles;
    public RoleQueryServiceImpl(RoleRepository roles) { this.roles = roles; }

    @Override @Transactional(readOnly = true)
    public List<Role> handle(IamQueries.GetAllRolesQuery query) { return roles.findAll(); }

    @Override @Transactional(readOnly = true)
    public Optional<Role> handle(IamQueries.GetRoleByNameQuery query) { return roles.findByName(query.name()); }
}
