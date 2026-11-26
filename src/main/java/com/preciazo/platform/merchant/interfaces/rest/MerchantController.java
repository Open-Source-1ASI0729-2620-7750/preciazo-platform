package com.preciazo.platform.merchant.interfaces.rest;

import com.preciazo.platform.merchant.application.internal.MerchantManagementService;
import com.preciazo.platform.merchant.infrastructure.persistence.jpa.entities.MerchantInventoryEntity;
import com.preciazo.platform.merchant.infrastructure.persistence.jpa.entities.MerchantPromotionEntity;
import com.preciazo.platform.merchant.infrastructure.persistence.jpa.entities.MerchantStoreEntity;
import com.preciazo.platform.shared.interfaces.rest.security.AuthenticatedPrincipal;
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
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/merchant", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Merchant Management", description = "Store profile, inventory stock/pricing and promotional campaign endpoints")
public class MerchantController {
    private final MerchantManagementService merchantService;

    public MerchantController(MerchantManagementService merchantService) {
        this.merchantService = merchantService;
    }

    public record UpdateStoreResource(
            @Schema(example = "Bodega Don Alberto S.A.C.") @NotBlank String businessName,
            @Schema(example = "20601234567") @NotBlank String ruc,
            @Schema(example = "Calle San Martín 380, Miraflores") @NotBlank String address,
            @Schema(example = "+51 1 610-8888") @NotBlank String phone,
            @Schema(example = "08:00 - 22:00") @NotBlank String scheduleWeek,
            @Schema(example = "08:00 - 21:00") @NotBlank String scheduleSat,
            @Schema(example = "09:00 - 19:00") @NotBlank String scheduleSun
    ) { }

    public record CreateInventoryResource(
            @Schema(example = "Yogurt Natural 1L") @NotBlank String productName,
            @Schema(example = "Lácteos") @NotBlank String category,
            @Schema(example = "6.50") @NotNull @DecimalMin("0.01") BigDecimal price,
            @Schema(example = "AVAILABLE") @Nullable String status
    ) { }

    public record UpdateInventoryResource(
            @Schema(example = "7.20") @Nullable BigDecimal price,
            @Schema(example = "OUT_OF_STOCK") @Nullable String status
    ) { }

    public record CreatePromotionResource(
            @Schema(example = "Leche Evaporada") @NotBlank String productName,
            @Schema(example = "2.80") @NotNull @DecimalMin("0.01") BigDecimal promoPrice,
            @Schema(example = "3.40") @Nullable BigDecimal originalPrice,
            @Nullable LocalDate startDate,
            @Nullable LocalDate endDate
    ) { }

    @GetMapping("/store")
    @Operation(summary = "Get merchant store profile", description = "Returns store registration details, RUC and operating hours.")
    public MerchantStoreEntity getStore(Principal principal) {
        AuthenticatedPrincipal.requireUserId(principal);
        return merchantService.getDefaultStore();
    }

    @PutMapping("/store")
    @Operation(summary = "Update merchant store profile", description = "Updates store name, RUC, address, phone and schedule.")
    public MerchantStoreEntity updateStore(@Valid @RequestBody UpdateStoreResource resource, Principal principal) {
        AuthenticatedPrincipal.requireUserId(principal);
        return merchantService.updateStore(resource.businessName(), resource.ruc(), resource.address(),
                resource.phone(), resource.scheduleWeek(), resource.scheduleSat(), resource.scheduleSun());
    }

    @GetMapping("/inventory")
    @Operation(summary = "List merchant inventory", description = "Returns all inventory items managed by the merchant store.")
    public List<MerchantInventoryEntity> listInventory(Principal principal) {
        AuthenticatedPrincipal.requireUserId(principal);
        return merchantService.listInventory();
    }

    @PostMapping("/inventory")
    @Operation(summary = "Add product to merchant inventory", description = "Creates a new inventory SKU with price and stock status.")
    public ResponseEntity<MerchantInventoryEntity> addInventory(@Valid @RequestBody CreateInventoryResource resource, Principal principal) {
        AuthenticatedPrincipal.requireUserId(principal);
        var saved = merchantService.addInventory(resource.productName(), resource.category(), resource.price(), resource.status());
        return ResponseEntity.created(URI.create("/api/v1/merchant/inventory/" + saved.getId())).body(saved);
    }

    @PatchMapping("/inventory/{itemId}")
    @Operation(summary = "Update inventory item price or availability", description = "Updates price and/or toggles AVAILABLE / OUT_OF_STOCK status.")
    public MerchantInventoryEntity updateInventory(@PathVariable long itemId, @RequestBody UpdateInventoryResource resource, Principal principal) {
        AuthenticatedPrincipal.requireUserId(principal);
        return merchantService.updateInventory(itemId, resource.price(), resource.status());
    }

    @GetMapping("/promotions")
    @Operation(summary = "List merchant promotions", description = "Returns active and historical promotional offers for the store.")
    public List<MerchantPromotionEntity> listPromotions(Principal principal) {
        AuthenticatedPrincipal.requireUserId(principal);
        return merchantService.listPromotions();
    }

    @PostMapping("/promotions")
    @Operation(summary = "Publish a new promotion", description = "Creates a time-bound promotional discount with automatic percentage calculation.")
    public ResponseEntity<MerchantPromotionEntity> createPromotion(@Valid @RequestBody CreatePromotionResource resource, Principal principal) {
        AuthenticatedPrincipal.requireUserId(principal);
        var saved = merchantService.createPromotion(resource.productName(), resource.promoPrice(),
                resource.originalPrice(), resource.startDate(), resource.endDate());
        return ResponseEntity.created(URI.create("/api/v1/merchant/promotions/" + saved.getId())).body(saved);
    }

    @DeleteMapping("/promotions/{promotionId}")
    @Operation(summary = "Delete a promotion", description = "Removes a promotion from the merchant store.")
    public ResponseEntity<Void> deletePromotion(@PathVariable long promotionId, Principal principal) {
        AuthenticatedPrincipal.requireUserId(principal);
        merchantService.deletePromotion(promotionId);
        return ResponseEntity.noContent().build();
    }
}
