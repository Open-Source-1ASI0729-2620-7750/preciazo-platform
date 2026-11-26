package com.preciazo.platform.iam.application.queryservices;

import com.preciazo.platform.iam.domain.model.aggregates.User;
import com.preciazo.platform.iam.domain.model.queries.IamQueries;
import java.util.List;
import java.util.Optional;

public interface UserQueryService {
    List<User> handle(IamQueries.GetAllUsersQuery query);
    Optional<User> handle(IamQueries.GetUserByIdQuery query);
    Optional<User> handle(IamQueries.GetUserByEmailQuery query);
}
