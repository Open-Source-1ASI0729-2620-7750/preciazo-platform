package com.preciazo.platform.iam.application.internal.queryservices;

import com.preciazo.platform.iam.application.queryservices.UserQueryService;
import com.preciazo.platform.iam.domain.model.aggregates.User;
import com.preciazo.platform.iam.domain.model.queries.IamQueries;
import com.preciazo.platform.iam.domain.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository users;
    public UserQueryServiceImpl(UserRepository users) { this.users = users; }

    @Override @Transactional(readOnly = true)
    public List<User> handle(IamQueries.GetAllUsersQuery query) { return users.findAll(); }

    @Override @Transactional(readOnly = true)
    public Optional<User> handle(IamQueries.GetUserByIdQuery query) { return users.findById(query.userId()); }

    @Override @Transactional(readOnly = true)
    public Optional<User> handle(IamQueries.GetUserByEmailQuery query) {
        return users.findByEmail(query.email().strip().toLowerCase(Locale.ROOT));
    }
}
