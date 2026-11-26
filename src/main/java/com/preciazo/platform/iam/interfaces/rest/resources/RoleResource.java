package com.preciazo.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

public record RoleResource(
        @Schema(example = "1") long id,
        @Schema(example = "ROLE_BUYER") String name
) { }
