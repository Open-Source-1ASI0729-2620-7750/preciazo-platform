package com.preciazo.platform.shopping.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shopping_list_budgets")
public class ShoppingListBudgetPersistenceEntity {
    @Id @Column(name = "shopping_list_id") private Long shoppingListId;
    @Column(name = "budget_limit", nullable = false, precision = 10, scale = 2) private BigDecimal budgetLimit;

    protected ShoppingListBudgetPersistenceEntity() { }
    public ShoppingListBudgetPersistenceEntity(Long shoppingListId, BigDecimal budgetLimit) {
        this.shoppingListId = shoppingListId;
        this.budgetLimit = budgetLimit;
    }
    public Long getShoppingListId() { return shoppingListId; }
    public BigDecimal getBudgetLimit() { return budgetLimit; }
}
