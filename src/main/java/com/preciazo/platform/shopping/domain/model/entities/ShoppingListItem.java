package com.preciazo.platform.shopping.domain.model.entities;

import org.jspecify.annotations.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Line item belonging to a ShoppingList aggregate. */
public final class ShoppingListItem {
    private final @Nullable Long id;
    private final long shoppingListId;
    private final String productName;
    private final int quantity;
    private final String unitLabel;
    private final BigDecimal unitPrice;
    private final boolean checked;

    public ShoppingListItem(@Nullable Long id, long shoppingListId, String productName, int quantity,
                            String unitLabel, BigDecimal unitPrice, boolean checked) {
        if (shoppingListId <= 0) throw new IllegalArgumentException("Shopping list id must be positive");
        if (productName == null || productName.isBlank() || productName.length() > 160) {
            throw new IllegalArgumentException("Invalid product name");
        }
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        this.id = id;
        this.shoppingListId = shoppingListId;
        this.productName = productName.strip();
        this.quantity = quantity;
        this.unitLabel = (unitLabel == null || unitLabel.isBlank()) ? "unid." : unitLabel.strip();
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
        this.checked = checked;
    }

    public @Nullable Long getId() { return id; }
    public long getShoppingListId() { return shoppingListId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public String getUnitLabel() { return unitLabel; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getSubtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP); }
    public boolean isChecked() { return checked; }
}
