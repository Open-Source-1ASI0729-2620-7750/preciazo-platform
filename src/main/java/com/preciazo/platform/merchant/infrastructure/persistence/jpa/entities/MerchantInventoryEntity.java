package com.preciazo.platform.merchant.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "merchant_inventory")
public class MerchantInventoryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "store_id", nullable = false) private long storeId;
    @Column(name = "product_name", nullable = false, length = 160) private String productName;
    @Column(nullable = false, length = 60) private String category;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal price;
    @Column(name = "unit_text", nullable = false, length = 40) private String unitText;
    @Column(nullable = false, length = 24) private String status;

    protected MerchantInventoryEntity() { }
    public MerchantInventoryEntity(Long id, long storeId, String productName, String category,
                                   BigDecimal price, String unitText, String status) {
        this.id = id; this.storeId = storeId; this.productName = productName;
        this.category = category; this.price = price; this.unitText = unitText; this.status = status;
    }
    public Long getId() { return id; }
    public long getStoreId() { return storeId; }
    public String getProductName() { return productName; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public String getUnitText() { return unitText; }
    public String getStatus() { return status; }
}
