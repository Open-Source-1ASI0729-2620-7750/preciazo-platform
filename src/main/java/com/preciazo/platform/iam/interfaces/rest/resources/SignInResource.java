package com.preciazo.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignInResource(
        @Schema(description = "Registered email address", example = "carlos.mendoza@preciazo.pe")
        @NotBlank @Email String email,
        @Schema(description = "Account password", example = "Preciazo2026!")
        @NotBlank String password
) { }
