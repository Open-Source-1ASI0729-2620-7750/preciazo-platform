package com.preciazo.platform.iam.interfaces.rest;

import com.preciazo.platform.iam.application.queryservices.UserQueryService;
import com.preciazo.platform.iam.domain.model.aggregates.User;
import com.preciazo.platform.iam.domain.model.queries.IamQueries;
import com.preciazo.platform.iam.interfaces.rest.resources.UserResource;
import com.preciazo.platform.shared.application.result.ApplicationError;
import com.preciazo.platform.shared.application.result.ApplicationErrorException;
import com.preciazo.platform.shared.interfaces.rest.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Authenticated user profile and directory endpoints")
public class UsersController {
    private final UserQueryService userQueryService;

    public UsersController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user", description = "Returns the profile and roles of the currently authenticated user.")
    public UserResource me(Principal principal) {
        long userId = AuthenticatedPrincipal.requireUserId(principal);
        User user = userQueryService.handle(new IamQueries.GetUserByIdQuery(userId))
                .orElseThrow(() -> new ApplicationErrorException(ApplicationError.notFound("user", Long.toString(userId))));
        return toResource(user);
    }

    @GetMapping
    @Operation(summary = "List registered users", description = "Returns all registered users (requires authentication).")
    public List<UserResource> getAllUsers(Principal principal) {
        AuthenticatedPrincipal.requireUserId(principal);
        return userQueryService.handle(new IamQueries.GetAllUsersQuery()).stream()
                .map(UsersController::toResource)
                .toList();
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Returns a user profile by its identifier (requires authentication).")
    public UserResource getUserById(@PathVariable long userId, Principal principal) {
        AuthenticatedPrincipal.requireUserId(principal);
        User user = userQueryService.handle(new IamQueries.GetUserByIdQuery(userId))
                .orElseThrow(() -> new ApplicationErrorException(ApplicationError.notFound("user", Long.toString(userId))));
        return toResource(user);
    }

    private static UserResource toResource(User user) {
        return new UserResource(Objects.requireNonNull(user.getId()), user.getEmail(),
                user.getFullName(), user.getRoleNames(), user.getCreatedAt());
    }
}
