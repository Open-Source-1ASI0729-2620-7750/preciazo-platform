package com.preciazo.platform.merchant.application.internal;

import com.preciazo.platform.merchant.infrastructure.persistence.jpa.entities.MerchantInventoryEntity;
import com.preciazo.platform.merchant.infrastructure.persistence.jpa.entities.MerchantPromotionEntity;
import com.preciazo.platform.merchant.infrastructure.persistence.jpa.entities.MerchantStoreEntity;
import com.preciazo.platform.merchant.infrastructure.persistence.jpa.repositories.MerchantInventoryRepository;
import com.preciazo.platform.merchant.infrastructure.persistence.jpa.repositories.MerchantPromotionRepository;
import com.preciazo.platform.merchant.infrastructure.persistence.jpa.repositories.MerchantStoreRepository;
import com.preciazo.platform.shared.application.result.ApplicationError;
import com.preciazo.platform.shared.application.result.ApplicationErrorException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class MerchantManagementService {
    private static final String DEFAULT_MERCHANT_EMAIL = "alberto.ramos@preciazo.pe";
    private final MerchantStoreRepository stores;
    private final MerchantInventoryRepository inventory;
    private final MerchantPromotionRepository promotions;

    public MerchantManagementService(MerchantStoreRepository stores,
                                     MerchantInventoryRepository inventory,
                                     MerchantPromotionRepository promotions) {
        this.stores = stores;
        this.inventory = inventory;
        this.promotions = promotions;
    }

    @Order(3)
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedMerchantIfEmpty() {
        if (stores.count() > 0) return;
        var store = stores.saveAndFlush(new MerchantStoreEntity(
                null, DEFAULT_MERCHANT_EMAIL, "Bodega Don Alberto S.A.C.", "20601234567",
                "Calle San Martín 380, Miraflores", "+51 1 610-8888",
                "08:00 - 22:00", "08:00 - 21:00", "09:00 - 19:00"));
        long storeId = store.getId();
        inventory.saveAllAndFlush(List.of(
                new MerchantInventoryEntity(null, storeId, "Leche Evaporada", "Lácteos", bd("3.20"), "S/ 3.20 / unid.", "AVAILABLE"),
                new MerchantInventoryEntity(null, storeId, "Arroz Extra 2kg", "Granos", bd("7.60"), "S/ 7.60 / unid.", "AVAILABLE"),
                new MerchantInventoryEntity(null, storeId, "Aceite Vegetal 1L", "Abarrotes", bd("8.70"), "S/ 8.70 / unid.", "AVAILABLE"),
                new MerchantInventoryEntity(null, storeId, "Azúcar Rubia 1kg", "Abarrotes", bd("4.50"), "S/ 4.50 / unid.", "AVAILABLE"),
                new MerchantInventoryEntity(null, storeId, "Detergente 500g", "Limpieza", bd("6.90"), "S/ 6.90 / unid.", "OUT_OF_STOCK")
        ));
        promotions.saveAllAndFlush(List.of(
                new MerchantPromotionEntity(null, storeId, "Aceite Vegetal 1L", bd("7.50"), bd("8.90"), 16, "ACTIVE", LocalDate.now().minusDays(2), LocalDate.now().plusDays(12), 124),
                new MerchantPromotionEntity(null, storeId, "Arroz Extra 2kg", bd("6.90"), bd("7.80"), 12, "ACTIVE", LocalDate.now().minusDays(1), LocalDate.now().plusDays(10), 89)
        ));
    }

    @Transactional(readOnly = true)
    public MerchantStoreEntity getDefaultStore() {
        return stores.findByOwnerEmail(DEFAULT_MERCHANT_EMAIL)
                .orElseGet(() -> stores.findAll().stream().findFirst()
                        .orElseThrow(() -> new ApplicationErrorException(ApplicationError.notFound("merchant_store", "default"))));
    }

    @Transactional
    public MerchantStoreEntity updateStore(String businessName, String ruc, String address, String phone,
                                           String scheduleWeek, String scheduleSat, String scheduleSun) {
        var current = getDefaultStore();
        return stores.saveAndFlush(new MerchantStoreEntity(
                current.getId(), current.getOwnerEmail(), businessName.strip(), ruc.strip(),
                address.strip(), phone.strip(), scheduleWeek.strip(), scheduleSat.strip(), scheduleSun.strip()));
    }

    @Transactional(readOnly = true)
    public List<MerchantInventoryEntity> listInventory() {
        return inventory.findAllByStoreIdOrderByIdAsc(getDefaultStore().getId());
    }

    @Transactional
    public MerchantInventoryEntity addInventory(String productName, String category, BigDecimal price, String status) {
        long storeId = getDefaultStore().getId();
        BigDecimal scaled = price.setScale(2, RoundingMode.HALF_UP);
        String normStatus = "OUT_OF_STOCK".equalsIgnoreCase(status) ? "OUT_OF_STOCK" : "AVAILABLE";
        return inventory.saveAndFlush(new MerchantInventoryEntity(
                null, storeId, productName.strip(), category.strip(), scaled,
                "S/ " + scaled + " / unid.", normStatus));
    }

    @Transactional
    public MerchantInventoryEntity updateInventory(long itemId, BigDecimal price, String status) {
        var item = inventory.findById(itemId)
                .orElseThrow(() -> new ApplicationErrorException(ApplicationError.notFound("inventory_item", Long.toString(itemId))));
        BigDecimal nextPrice = price != null ? price.setScale(2, RoundingMode.HALF_UP) : item.getPrice();
        String nextStatus = status != null && !status.isBlank()
                ? ("OUT_OF_STOCK".equalsIgnoreCase(status) ? "OUT_OF_STOCK" : "AVAILABLE")
                : item.getStatus();
        return inventory.saveAndFlush(new MerchantInventoryEntity(
                item.getId(), item.getStoreId(), item.getProductName(), item.getCategory(),
                nextPrice, "S/ " + nextPrice + " / unid.", nextStatus));
    }

    @Transactional(readOnly = true)
    public List<MerchantPromotionEntity> listPromotions() {
        return promotions.findAllByStoreIdOrderByIdDesc(getDefaultStore().getId());
    }

    @Transactional
    public MerchantPromotionEntity createPromotion(String productName, BigDecimal promoPrice,
                                                   BigDecimal originalPrice, LocalDate startDate, LocalDate endDate) {
        if (promoPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApplicationErrorException(ApplicationError.validationError("promoPrice", "Promotional price must be positive"));
        }
        BigDecimal orig = (originalPrice != null && originalPrice.compareTo(promoPrice) > 0)
                ? originalPrice.setScale(2, RoundingMode.HALF_UP)
                : promoPrice.multiply(new BigDecimal("1.20")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal promo = promoPrice.setScale(2, RoundingMode.HALF_UP);
        int pct = orig.subtract(promo).multiply(BigDecimal.valueOf(100)).divide(orig, 0, RoundingMode.HALF_UP).intValue();
        LocalDate start = startDate != null ? startDate : LocalDate.now();
        LocalDate end = endDate != null ? endDate : start.plusDays(14);
        if (end.isBefore(start)) {
            throw new ApplicationErrorException(ApplicationError.validationError("endDate", "End date cannot precede start date"));
        }
        return promotions.saveAndFlush(new MerchantPromotionEntity(
                null, getDefaultStore().getId(), productName.strip(), promo, orig, pct, "ACTIVE", start, end, 0));
    }

    @Transactional
    public void deletePromotion(long promotionId) {
        var existing = promotions.findById(promotionId)
                .orElseThrow(() -> new ApplicationErrorException(ApplicationError.notFound("promotion", Long.toString(promotionId))));
        promotions.delete(existing);
    }

    private static BigDecimal bd(String v) { return new BigDecimal(v); }
}
