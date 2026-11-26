package com.preciazo.platform.catalog.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "catalog_store_prices")
public class CatalogStorePriceEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "product_name", nullable = false, length = 160) private String productName;
    @Column(nullable = false, length = 60) private String category;
    @Column(nullable = false, length = 20) private String unit;
    @Column(name = "store_name", nullable = false, length = 120) private String storeName;
    @Column(name = "store_type", nullable = false, length = 60) private String storeType;
    @Column(name = "store_address", nullable = false, length = 200) private String storeAddress;
    @Column(name = "distance_km", nullable = false) private double distanceKm;
    @Column(nullable = false) private double latitude;
    @Column(nullable = false) private double longitude;
    @Column(nullable = false) private double rating;
    @Column(name = "is_local", nullable = false) private boolean localStore;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal price;
    @Column(name = "original_price", precision = 10, scale = 2) private BigDecimal originalPrice;
    @Column(name = "unit_price_label", nullable = false, length = 40) private String unitPriceLabel;
    @Column(nullable = false, length = 24) private String status;

    protected CatalogStorePriceEntity() { }

    public CatalogStorePriceEntity(Long id, String productName, String category, String unit, String storeName,
                                   String storeType, String storeAddress, double distanceKm, double latitude,
                                   double longitude, double rating, boolean localStore, BigDecimal price,
                                   BigDecimal originalPrice, String unitPriceLabel, String status) {
        this.id = id; this.productName = productName; this.category = category; this.unit = unit;
        this.storeName = storeName; this.storeType = storeType; this.storeAddress = storeAddress;
        this.distanceKm = distanceKm; this.latitude = latitude; this.longitude = longitude;
        this.rating = rating; this.localStore = localStore; this.price = price;
        this.originalPrice = originalPrice; this.unitPriceLabel = unitPriceLabel; this.status = status;
    }

    public Long getId() { return id; }
    public String getProductName() { return productName; }
    public String getCategory() { return category; }
    public String getUnit() { return unit; }
    public String getStoreName() { return storeName; }
    public String getStoreType() { return storeType; }
    public String getStoreAddress() { return storeAddress; }
    public double getDistanceKm() { return distanceKm; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getRating() { return rating; }
    public boolean isLocalStore() { return localStore; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public String getUnitPriceLabel() { return unitPriceLabel; }
    public String getStatus() { return status; }
}
