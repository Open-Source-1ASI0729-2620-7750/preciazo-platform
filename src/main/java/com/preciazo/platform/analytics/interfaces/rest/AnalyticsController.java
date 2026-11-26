package com.preciazo.platform.analytics.interfaces.rest;

import com.preciazo.platform.merchant.application.internal.MerchantManagementService;
import com.preciazo.platform.merchant.infrastructure.persistence.jpa.entities.MerchantPromotionEntity;
import com.preciazo.platform.shared.interfaces.rest.security.AuthenticatedPrincipal;
import com.preciazo.platform.verification.application.internal.PriceVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping(value = "/api/v1/analytics", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Analytics & Engagement", description = "Merchant dashboard metrics and platform engagement indicators")
public class AnalyticsController {
    private final MerchantManagementService merchantService;
    private final PriceVerificationService verificationService;

    public AnalyticsController(MerchantManagementService merchantService,
                               PriceVerificationService verificationService) {
        this.merchantService = merchantService;
        this.verificationService = verificationService;
    }

    public record MerchantAnalyticsResource(
            String storeName,
            int totalInventoryItems,
            long availableItems,
            long outOfStockItems,
            int activePromotions,
            int totalPromotionConsults,
            long pendingCommunityVerifications,
            int estimatedMonthlyVisits
    ) { }

    @GetMapping("/merchant-summary")
    @Operation(summary = "Get merchant analytics summary", description = "Aggregates live inventory availability, promotion engagement and community verification counts.")
    public MerchantAnalyticsResource getMerchantSummary(Principal principal) {
        AuthenticatedPrincipal.requireUserId(principal);
        var store = merchantService.getDefaultStore();
        var inv = merchantService.listInventory();
        var promos = merchantService.listPromotions();
        var verifications = verificationService.listAll();

        long available = inv.stream().filter(i -> "AVAILABLE".equals(i.getStatus())).count();
        long outOfStock = inv.size() - available;
        int activePromos = (int) promos.stream().filter(p -> "ACTIVE".equals(p.getStatus())).count();
        int totalConsults = promos.stream().mapToInt(MerchantPromotionEntity::getConsultsCount).sum();
        long pendingVerifs = verifications.stream().filter(v -> "PENDING".equals(v.getStatus())).count();

        return new MerchantAnalyticsResource(
                store.getBusinessName(),
                inv.size(),
                available,
                outOfStock,
                activePromos,
                totalConsults,
                pendingVerifs,
                340 + totalConsults
        );
    }
}
