package com.preciazo.platform.verification.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "price_verifications")
public class PriceVerificationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "store_name", nullable = false, length = 120) private String storeName;
    @Column(name = "product_name", nullable = false, length = 160) private String productName;
    @Column(name = "published_price", nullable = false, precision = 10, scale = 2) private BigDecimal publishedPrice;
    @Column(name = "reported_price", nullable = false, precision = 10, scale = 2) private BigDecimal reportedPrice;
    @Column(name = "confirmations_count", nullable = false) private int confirmationsCount;
    @Column(name = "rejections_count", nullable = false) private int rejectionsCount;
    @Column(nullable = false, length = 24) private String status;
    @Column(name = "reported_by_user_id", nullable = false) private long reportedByUserId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected PriceVerificationEntity() { }

    public PriceVerificationEntity(Long id, String storeName, String productName, BigDecimal publishedPrice,
                                   BigDecimal reportedPrice, int confirmationsCount, int rejectionsCount,
                                   String status, long reportedByUserId, Instant createdAt) {
        this.id = id; this.storeName = storeName; this.productName = productName;
        this.publishedPrice = publishedPrice; this.reportedPrice = reportedPrice;
        this.confirmationsCount = confirmationsCount; this.rejectionsCount = rejectionsCount;
        this.status = status; this.reportedByUserId = reportedByUserId; this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getStoreName() { return storeName; }
    public String getProductName() { return productName; }
    public BigDecimal getPublishedPrice() { return publishedPrice; }
    public BigDecimal getReportedPrice() { return reportedPrice; }
    public int getConfirmationsCount() { return confirmationsCount; }
    public int getRejectionsCount() { return rejectionsCount; }
    public String getStatus() { return status; }
    public long getReportedByUserId() { return reportedByUserId; }
    public Instant getCreatedAt() { return createdAt; }
}
