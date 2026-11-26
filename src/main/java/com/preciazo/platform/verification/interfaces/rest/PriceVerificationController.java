package com.preciazo.platform.verification.interfaces.rest;

import com.preciazo.platform.shared.interfaces.rest.security.AuthenticatedPrincipal;
import com.preciazo.platform.verification.application.internal.PriceVerificationService;
import com.preciazo.platform.verification.infrastructure.persistence.jpa.entities.PriceVerificationEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/verifications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Community Price Verification", description = "Crowd-sourced shelf price reporting and community validation")
public class PriceVerificationController {
    private final PriceVerificationService service;

    public PriceVerificationController(PriceVerificationService service) {
        this.service = service;
    }

    public record CreateVerificationResource(
            @Schema(example = "Metro Miraflores") @NotBlank String storeName,
            @Schema(example = "Arroz Extra 2kg") @NotBlank String productName,
            @Schema(example = "7.80") @Nullable BigDecimal publishedPrice,
            @Schema(example = "7.40") @NotNull @DecimalMin("0.01") BigDecimal reportedPrice
    ) { }

    @GetMapping
    @Operation(summary = "List community price verification reports", description = "Returns recent shelf price reports ordered from newest to oldest.")
    public List<PriceVerificationEntity> listReports() {
        return service.listAll();
    }

    @PostMapping
    @Operation(summary = "Submit a new shelf price report", description = "Allows an authenticated buyer to report an observed shelf price.")
    public ResponseEntity<PriceVerificationEntity> createReport(@Valid @RequestBody CreateVerificationResource resource, Principal principal) {
        long userId = AuthenticatedPrincipal.requireUserId(principal);
        var saved = service.createReport(userId, resource.storeName(), resource.productName(),
                resource.publishedPrice(), resource.reportedPrice());
        return ResponseEntity.created(URI.create("/api/v1/verifications/" + saved.getId())).body(saved);
    }

    @PostMapping("/{reportId}/confirm")
    @Operation(summary = "Confirm a price report", description = "Confirms that the reported shelf price matches the store price.")
    public PriceVerificationEntity confirm(@PathVariable long reportId, Principal principal) {
        AuthenticatedPrincipal.requireUserId(principal);
        return service.vote(reportId, true);
    }

    @PostMapping("/{reportId}/reject")
    @Operation(summary = "Reject a price report", description = "Marks a reported price as not matching the observed store price.")
    public PriceVerificationEntity reject(@PathVariable long reportId, Principal principal) {
        AuthenticatedPrincipal.requireUserId(principal);
        return service.vote(reportId, false);
    }
}
