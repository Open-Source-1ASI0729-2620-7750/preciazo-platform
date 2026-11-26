package com.preciazo.platform.catalog.interfaces.rest;

import com.preciazo.platform.catalog.application.internal.CatalogQueryService;
import com.preciazo.platform.catalog.infrastructure.persistence.jpa.entities.CatalogStorePriceEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping(value = "/api/v1/catalog", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Catalog & Pricing", description = "Public product catalog, nearby stores, featured deals and multi-store basket comparison")
public class CatalogController {
    private final CatalogQueryService catalogService;

    public CatalogController(CatalogQueryService catalogService) {
        this.catalogService = catalogService;
    }

    public record ProductPriceResource(
            long id, String productName, String category, String unit, String storeName, String storeAddress,
            double distanceKm, BigDecimal price, BigDecimal originalPrice, String unitPriceLabel, String status
    ) { }

    public record StoreSummaryResource(
            String name, String type, String address, double distanceKm, double latitude, double longitude,
            double rating, boolean localStore, long productsCount, boolean open
    ) { }

    public record DealResource(
            long id, String store, String productName, String category, BigDecimal price, BigDecimal oldPrice, String discountBadge
    ) { }

    public record StoreComparisonRank(int rank, String storeName, double distanceKm, BigDecimal basketTotal,
                                      int availableItems, int totalItems, boolean bestOption) { }

    public record ProductStoreOffer(String storeName, BigDecimal price, String unitPriceLabel, String status, boolean bestPrice) { }

    public record BasketComparisonRow(String productName, String unit, List<ProductStoreOffer> offers) { }

    public record BasketComparisonResource(List<StoreComparisonRank> storeRankings, List<BasketComparisonRow> items) { }

    @GetMapping("/products")
    @Operation(summary = "Search catalog products and prices", description = "Filters products by category, store name, or search text.")
    public List<ProductPriceResource> listProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String q) {
        return catalogService.findProducts(category, store, q).stream()
                .map(p -> new ProductPriceResource(p.getId(), p.getProductName(), p.getCategory(), p.getUnit(),
                        p.getStoreName(), p.getStoreAddress(), p.getDistanceKm(), p.getPrice(), p.getOriginalPrice(),
                        p.getUnitPriceLabel(), p.getStatus()))
                .toList();
    }

    @GetMapping("/stores")
    @Operation(summary = "List nearby stores", description = "Returns affiliated supermarkets and local neighborhood stores sorted by distance.")
    public List<StoreSummaryResource> listStores(@RequestParam(required = false) Double maxRadiusKm) {
        Map<String, List<CatalogStorePriceEntity>> byStore = new LinkedHashMap<>();
        for (var entry : catalogService.findProducts(null, null, null)) {
            if (maxRadiusKm != null && entry.getDistanceKm() > maxRadiusKm) continue;
            byStore.computeIfAbsent(entry.getStoreName(), _ -> new ArrayList<>()).add(entry);
        }
        return byStore.values().stream()
                .map(list -> {
                    var s = list.getFirst();
                    return new StoreSummaryResource(s.getStoreName(), s.getStoreType(), s.getStoreAddress(),
                            s.getDistanceKm(), s.getLatitude(), s.getLongitude(), s.getRating(),
                            s.isLocalStore(), list.size(), true);
                })
                .sorted(Comparator.comparingDouble(StoreSummaryResource::distanceKm))
                .toList();
    }

    @GetMapping("/deals")
    @Operation(summary = "List featured deals", description = "Returns top discounted products available across nearby stores.")
    public List<DealResource> listDeals() {
        return catalogService.findProducts(null, null, null).stream()
                .filter(p -> !"OUT_OF_STOCK".equals(p.getStatus()) && p.getOriginalPrice() != null
                        && p.getOriginalPrice().compareTo(p.getPrice()) > 0)
                .sorted(Comparator.comparingInt(CatalogController::discountPercent).reversed())
                .limit(6)
                .map(p -> new DealResource(p.getId(), p.getStoreName(), p.getProductName(), p.getCategory(),
                        p.getPrice(), p.getOriginalPrice(), "-" + discountPercent(p) + "%"))
                .toList();
    }

    @GetMapping("/compare")
    @Operation(summary = "Compare multi-store basket prices", description = "Computes per-store totals, availability counts and lowest-price highlights across stores.")
    public BasketComparisonResource compareBasket(
            @RequestParam(required = false) Double maxRadiusKm,
            @RequestParam(required = false) List<String> stores) {
        Set<String> allowedStores = stores == null ? Set.of() :
                stores.stream().map(s -> s.strip().toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toSet());
        List<CatalogStorePriceEntity> filtered = catalogService.findProducts(null, null, null).stream()
                .filter(p -> maxRadiusKm == null || p.getDistanceKm() <= maxRadiusKm)
                .filter(p -> allowedStores.isEmpty() || allowedStores.contains(p.getStoreName().toLowerCase(Locale.ROOT)))
                .toList();

        Map<String, List<CatalogStorePriceEntity>> byProduct = new LinkedHashMap<>();
        Map<String, List<CatalogStorePriceEntity>> byStore = new LinkedHashMap<>();
        for (var p : filtered) {
            byProduct.computeIfAbsent(p.getProductName(), _ -> new ArrayList<>()).add(p);
            byStore.computeIfAbsent(p.getStoreName(), _ -> new ArrayList<>()).add(p);
        }

        List<BasketComparisonRow> rows = new ArrayList<>();
        for (var entry : byProduct.entrySet()) {
            BigDecimal minPrice = entry.getValue().stream()
                    .filter(p -> !"OUT_OF_STOCK".equals(p.getStatus()) && p.getPrice().compareTo(BigDecimal.ZERO) > 0)
                    .map(CatalogStorePriceEntity::getPrice)
                    .min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
            List<ProductStoreOffer> offers = entry.getValue().stream()
                    .map(p -> new ProductStoreOffer(p.getStoreName(), p.getPrice(), p.getUnitPriceLabel(), p.getStatus(),
                            !"OUT_OF_STOCK".equals(p.getStatus()) && p.getPrice().compareTo(minPrice) == 0))
                    .toList();
            rows.add(new BasketComparisonRow(entry.getKey(), entry.getValue().getFirst().getUnit(), offers));
        }

        int totalProducts = byProduct.size();
        record TempRank(String store, double dist, BigDecimal total, int avail) { }
        List<TempRank> sorted = byStore.entrySet().stream()
                .map(e -> {
                    int avail = (int) e.getValue().stream().filter(p -> !"OUT_OF_STOCK".equals(p.getStatus())).count();
                    BigDecimal sum = e.getValue().stream()
                            .filter(p -> !"OUT_OF_STOCK".equals(p.getStatus()))
                            .map(CatalogStorePriceEntity::getPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
                    return new TempRank(e.getKey(), e.getValue().getFirst().getDistanceKm(), sum, avail);
                })
                .sorted(Comparator.comparingInt(TempRank::avail).reversed().thenComparing(TempRank::total))
                .toList();

        List<StoreComparisonRank> ranks = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            var r = sorted.get(i);
            ranks.add(new StoreComparisonRank(i + 1, r.store(), r.dist(), r.total(), r.avail(), totalProducts, i == 0));
        }
        return new BasketComparisonResource(ranks, rows);
    }

    private static int discountPercent(CatalogStorePriceEntity p) {
        if (p.getOriginalPrice() == null || p.getOriginalPrice().compareTo(BigDecimal.ZERO) <= 0) return 0;
        return p.getOriginalPrice().subtract(p.getPrice())
                .multiply(BigDecimal.valueOf(100))
                .divide(p.getOriginalPrice(), 0, RoundingMode.HALF_UP).intValue();
    }
}
