package com.preciazo.platform.iam.application.internal.commandservices;

import com.preciazo.platform.iam.application.commandservices.UserCommandService;
import com.preciazo.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.preciazo.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.preciazo.platform.iam.domain.model.aggregates.User;
import com.preciazo.platform.iam.domain.model.commands.SignInCommand;
import com.preciazo.platform.iam.domain.model.commands.SignUpCommand;
import com.preciazo.platform.iam.domain.model.entities.Role;
import com.preciazo.platform.iam.domain.model.valueobjects.Roles;
import com.preciazo.platform.iam.domain.repositories.RoleRepository;
import com.preciazo.platform.iam.domain.repositories.UserRepository;
import com.preciazo.platform.shared.application.result.ApplicationError;
import com.preciazo.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository users;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;

    public UserCommandServiceImpl(UserRepository users, RoleRepository roleRepository,
                                  HashingService hashingService, TokenService tokenService) {
        this.users = users;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
    }

    @Override @Transactional
    public Result<User, ApplicationError> handle(SignUpCommand command) {
        if (command.email() == null || command.email().isBlank() || !command.email().contains("@")) {
            return Result.failure(ApplicationError.validationError("email", "Valid email is required"));
        }
        if (command.password() == null || command.password().length() < 6) {
            return Result.failure(ApplicationError.validationError("password", "Password must be at least 6 characters"));
        }
        String normalizedEmail = command.email().strip().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(normalizedEmail)) {
            return Result.failure(ApplicationError.conflict("user", "Email already registered"));
        }
        List<String> requestedRoles = (command.roles() == null || command.roles().isEmpty())
                ? List.of(Roles.ROLE_BUYER.name()) : command.roles();
        Set<Role> resolvedRoles = new LinkedHashSet<>();
        for (String roleName : requestedRoles) {
            Roles enumRole;
            try {
                enumRole = Roles.valueOf(roleName.strip().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                return Result.failure(ApplicationError.validationError("roles", "Unknown role: " + roleName));
            }
            Role role = roleRepository.findByName(enumRole)
                    .orElseGet(() -> roleRepository.save(Role.create(enumRole)));
            resolvedRoles.add(role);
        }
        String fullName = (command.fullName() == null || command.fullName().isBlank())
                ? normalizedEmail.substring(0, normalizedEmail.indexOf('@')) : command.fullName().strip();
        String hash = hashingService.encode(command.password());
        User created = User.create(normalizedEmail, fullName, hash, resolvedRoles, Instant.now());
        return Result.success(users.save(created));
    }

    @Override @Transactional(readOnly = true)
    public Result<AuthenticatedUser, ApplicationError> handle(SignInCommand command) {
        if (command.email() == null || command.email().isBlank() || command.password() == null || command.password().isBlank()) {
            return Result.failure(new ApplicationError("INVALID_CREDENTIALS", "Invalid email or password"));
        }
        String normalizedEmail = command.email().strip().toLowerCase(Locale.ROOT);
        var userOpt = users.findByEmail(normalizedEmail);
        if (userOpt.isEmpty() || !hashingService.matches(command.password(), userOpt.get().getPasswordHash())) {
            return Result.failure(new ApplicationError("INVALID_CREDENTIALS", "Invalid email or password"));
        }
        User user = userOpt.get();
        String token = tokenService.generateToken(Objects.requireNonNull(user.getId()), user.getEmail(), user.getRoleNames());
        return Result.success(new AuthenticatedUser(user, token));
    }
}
