package com.preciazo.platform.shopping.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shopping_list_items", indexes = @Index(name = "idx_shopping_items_list", columnList = "shopping_list_id"))
public class ShoppingListItemPersistenceEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "shopping_list_id", nullable = false) private long shoppingListId;
    @Column(name = "product_name", nullable = false, length = 160) private String productName;
    @Column(nullable = false) private int quantity;
    @Column(name = "unit_label", nullable = false, length = 40) private String unitLabel;
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2) private BigDecimal unitPrice;
    @Column(nullable = false) private boolean checked;

    protected ShoppingListItemPersistenceEntity() { }
    public ShoppingListItemPersistenceEntity(Long id, long shoppingListId, String productName, int quantity,
                                             String unitLabel, BigDecimal unitPrice, boolean checked) {
        this.id = id; this.shoppingListId = shoppingListId; this.productName = productName;
        this.quantity = quantity; this.unitLabel = unitLabel; this.unitPrice = unitPrice; this.checked = checked;
    }
    public Long getId() { return id; }
    public long getShoppingListId() { return shoppingListId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public String getUnitLabel() { return unitLabel; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public boolean isChecked() { return checked; }
}
