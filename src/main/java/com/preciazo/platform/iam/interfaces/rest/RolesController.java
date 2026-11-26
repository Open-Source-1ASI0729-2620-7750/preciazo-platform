package com.preciazo.platform.iam.interfaces.rest;

import com.preciazo.platform.iam.application.queryservices.RoleQueryService;
import com.preciazo.platform.iam.domain.model.queries.IamQueries;
import com.preciazo.platform.iam.interfaces.rest.resources.RoleResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/v1/roles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Roles", description = "Available IAM role definitions")
public class RolesController {
    private final RoleQueryService roleQueryService;

    public RolesController(RoleQueryService roleQueryService) {
        this.roleQueryService = roleQueryService;
    }

    @GetMapping
    @Operation(summary = "List all available roles", description = "Returns the seeded platform roles (ROLE_BUYER, ROLE_MERCHANT, ROLE_ADMIN).")
    public List<RoleResource> getAllRoles() {
        return roleQueryService.handle(new IamQueries.GetAllRolesQuery()).stream()
                .map(r -> new RoleResource(Objects.requireNonNull(r.getId()), r.getStringName()))
                .toList();
    }
}
