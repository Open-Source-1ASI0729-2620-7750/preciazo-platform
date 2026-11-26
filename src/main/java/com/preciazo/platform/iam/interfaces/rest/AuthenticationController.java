package com.preciazo.platform.iam.interfaces.rest;

import com.preciazo.platform.iam.application.commandservices.UserCommandService;
import com.preciazo.platform.iam.domain.model.aggregates.User;
import com.preciazo.platform.iam.domain.model.commands.SignInCommand;
import com.preciazo.platform.iam.domain.model.commands.SignUpCommand;
import com.preciazo.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.preciazo.platform.iam.interfaces.rest.resources.SignInResource;
import com.preciazo.platform.iam.interfaces.rest.resources.SignUpResource;
import com.preciazo.platform.iam.interfaces.rest.resources.UserResource;
import com.preciazo.platform.shared.application.result.ApplicationError;
import com.preciazo.platform.shared.application.result.ApplicationErrorException;
import com.preciazo.platform.shared.application.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "User registration (sign-up) and JWT authentication (sign-in) endpoints")
public class AuthenticationController {
    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @PostMapping("/sign-up")
    @Operation(summary = "Register a new user account", description = "Creates a new Buyer or Merchant user account and persists hashed credentials.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    public ResponseEntity<UserResource> signUp(@Valid @RequestBody SignUpResource resource) {
        var result = userCommandService.handle(new SignUpCommand(
                resource.email(), resource.fullName(), resource.password(), resource.roles()));
        if (result instanceof Result.Failure<User, ApplicationError> failure) {
            throw new ApplicationErrorException(failure.error());
        }
        User user = result.toOptional().orElseThrow();
        long id = Objects.requireNonNull(user.getId());
        return ResponseEntity.created(URI.create("/api/v1/users/" + id))
                .body(new UserResource(id, user.getEmail(), user.getFullName(), user.getRoleNames(), user.getCreatedAt()));
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Authenticate user and obtain JWT", description = "Validates email and password and returns a signed JWT Bearer token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public ResponseEntity<AuthenticatedUserResource> signIn(@Valid @RequestBody SignInResource resource) {
        var result = userCommandService.handle(new SignInCommand(resource.email(), resource.password()));
        if (result instanceof Result.Failure<UserCommandService.AuthenticatedUser, ApplicationError> failure) {
            throw new ApplicationErrorException(failure.error());
        }
        var auth = result.toOptional().orElseThrow();
        User u = auth.user();
        return ResponseEntity.ok(new AuthenticatedUserResource(
                Objects.requireNonNull(u.getId()), u.getEmail(), u.getFullName(), u.getRoleNames(), auth.token()));
    }
}
