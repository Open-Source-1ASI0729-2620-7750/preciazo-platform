package com.preciazo.platform.iam.application.queryservices;

import com.preciazo.platform.iam.domain.model.entities.Role;
import com.preciazo.platform.iam.domain.model.queries.IamQueries;
import java.util.List;
import java.util.Optional;

public interface RoleQueryService {
    List<Role> handle(IamQueries.GetAllRolesQuery query);
    Optional<Role> handle(IamQueries.GetRoleByNameQuery query);
}
