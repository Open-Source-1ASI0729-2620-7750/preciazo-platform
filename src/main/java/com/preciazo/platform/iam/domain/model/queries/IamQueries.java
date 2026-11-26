package com.preciazo.platform.iam.domain.model.queries;

import com.preciazo.platform.iam.domain.model.valueobjects.Roles;

public final class IamQueries {
    private IamQueries() { }
    public record GetAllRolesQuery() { }
    public record GetRoleByNameQuery(Roles name) { }
    public record GetAllUsersQuery() { }
    public record GetUserByIdQuery(long userId) { }
    public record GetUserByEmailQuery(String email) { }
}
