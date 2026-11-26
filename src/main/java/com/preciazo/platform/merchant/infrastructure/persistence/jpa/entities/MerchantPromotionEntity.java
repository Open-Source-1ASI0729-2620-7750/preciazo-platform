package com.preciazo.platform.merchant.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "merchant_promotions")
public class MerchantPromotionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "store_id", nullable = false) private long storeId;
    @Column(name = "product_name", nullable = false, length = 160) private String productName;
    @Column(name = "promo_price", nullable = false, precision = 10, scale = 2) private BigDecimal promoPrice;
    @Column(name = "original_price", nullable = false, precision = 10, scale = 2) private BigDecimal originalPrice;
    @Column(name = "discount_percent", nullable = false) private int discountPercent;
    @Column(nullable = false, length = 24) private String status;
    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(name = "end_date", nullable = false) private LocalDate endDate;
    @Column(name = "consults_count", nullable = false) private int consultsCount;

    protected MerchantPromotionEntity() { }
    public MerchantPromotionEntity(Long id, long storeId, String productName, BigDecimal promoPrice,
                                   BigDecimal originalPrice, int discountPercent, String status,
                                   LocalDate startDate, LocalDate endDate, int consultsCount) {
        this.id = id; this.storeId = storeId; this.productName = productName; this.promoPrice = promoPrice;
        this.originalPrice = originalPrice; this.discountPercent = discountPercent; this.status = status;
        this.startDate = startDate; this.endDate = endDate; this.consultsCount = consultsCount;
    }
    public Long getId() { return id; }
    public long getStoreId() { return storeId; }
    public String getProductName() { return productName; }
    public BigDecimal getPromoPrice() { return promoPrice; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public int getDiscountPercent() { return discountPercent; }
    public String getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public int getConsultsCount() { return consultsCount; }
}
