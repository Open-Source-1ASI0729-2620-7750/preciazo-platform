package com.preciazo.platform.iam.domain.repositories;

import com.preciazo.platform.iam.domain.model.aggregates.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(long id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAll();
    User save(User user);
}
