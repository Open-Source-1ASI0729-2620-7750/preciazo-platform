package com.preciazo.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SignUpResource(
        @Schema(description = "Unique user email address", example = "maria.lopez@preciazo.pe")
        @NotBlank @Email String email,
        @Schema(description = "Full name of the user", example = "María López")
        @NotBlank @Size(max = 160) String fullName,
        @Schema(description = "Account password (min 6 characters)", example = "Preciazo2026!")
        @NotBlank @Size(min = 6, max = 100) String password,
        @Schema(description = "Assigned roles (defaults to ROLE_BUYER)", example = "[\"ROLE_BUYER\"]")
        List<String> roles
) { }
