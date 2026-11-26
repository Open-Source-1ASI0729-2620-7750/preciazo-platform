package com.preciazo.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

public record UserResource(
        @Schema(example = "1") long id,
        @Schema(example = "carlos.mendoza@preciazo.pe") String email,
        @Schema(example = "Carlos Mendoza") String fullName,
        @Schema(example = "[\"ROLE_BUYER\"]") List<String> roles,
        Instant createdAt
) { }
