package com.preciazo.platform.shopping.application.internal;

import com.preciazo.platform.shared.application.result.ApplicationError;
import com.preciazo.platform.shared.application.result.ApplicationErrorException;
import com.preciazo.platform.shopping.domain.model.entities.ShoppingListItem;
import com.preciazo.platform.shopping.domain.repositories.ShoppingListRepository;
import com.preciazo.platform.shopping.infrastructure.persistence.jpa.entities.ShoppingListBudgetPersistenceEntity;
import com.preciazo.platform.shopping.infrastructure.persistence.jpa.entities.ShoppingListItemPersistenceEntity;
import com.preciazo.platform.shopping.infrastructure.persistence.jpa.repositories.ShoppingListBudgetPersistenceRepository;
import com.preciazo.platform.shopping.infrastructure.persistence.jpa.repositories.ShoppingListItemPersistenceRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ShoppingListItemsService {
    private static final BigDecimal DEFAULT_BUDGET = new BigDecimal("150.00");
    private final ShoppingListRepository lists;
    private final ShoppingListItemPersistenceRepository items;
    private final ShoppingListBudgetPersistenceRepository budgets;

    public ShoppingListItemsService(ShoppingListRepository lists,
                                    ShoppingListItemPersistenceRepository items,
                                    ShoppingListBudgetPersistenceRepository budgets) {
        this.lists = lists;
        this.items = items;
        this.budgets = budgets;
    }

    private void assertOwner(long ownerId, long listId) {
        var list = lists.findById(listId)
                .orElseThrow(() -> new ApplicationErrorException(ApplicationError.notFound("shopping_list", Long.toString(listId))));
        if (list.getOwnerId() != ownerId) {
            throw new ApplicationErrorException(new ApplicationError("SHOPPING_FORBIDDEN", "Not the owner of this shopping list"));
        }
    }

    @Transactional(readOnly = true)
    public List<ShoppingListItem> listItems(long ownerId, long listId) {
        assertOwner(ownerId, listId);
        return items.findAllByShoppingListIdOrderByIdAsc(listId).stream().map(this::toDomain).toList();
    }

    @Transactional
    public ShoppingListItem addItem(long ownerId, long listId, String productName, int quantity,
                                    @Nullable String unitLabel, BigDecimal unitPrice) {
        assertOwner(ownerId, listId);
        ShoppingListItem domain;
        try {
            domain = new ShoppingListItem(null, listId, productName, quantity, unitLabel == null ? "unid." : unitLabel, unitPrice, false);
        } catch (IllegalArgumentException ex) {
            throw new ApplicationErrorException(ApplicationError.validationError("shoppingListItem", ex.getMessage()));
        }
        var saved = items.saveAndFlush(new ShoppingListItemPersistenceEntity(
                null, domain.getShoppingListId(), domain.getProductName(), domain.getQuantity(),
                domain.getUnitLabel(), domain.getUnitPrice(), domain.isChecked()));
        return toDomain(saved);
    }

    @Transactional
    public ShoppingListItem updateItem(long ownerId, long listId, long itemId, @Nullable String productName,
                                       @Nullable Integer quantity, @Nullable BigDecimal unitPrice, @Nullable Boolean checked) {
        assertOwner(ownerId, listId);
        var existing = items.findByIdAndShoppingListId(itemId, listId)
                .orElseThrow(() -> new ApplicationErrorException(ApplicationError.notFound("shopping_list_item", Long.toString(itemId))));
        ShoppingListItem updated;
        try {
            updated = new ShoppingListItem(
                    existing.getId(), listId,
                    productName != null ? productName : existing.getProductName(),
                    quantity != null ? quantity : existing.getQuantity(),
                    existing.getUnitLabel(),
                    unitPrice != null ? unitPrice : existing.getUnitPrice(),
                    checked != null ? checked : existing.isChecked());
        } catch (IllegalArgumentException ex) {
            throw new ApplicationErrorException(ApplicationError.validationError("shoppingListItem", ex.getMessage()));
        }
        var saved = items.saveAndFlush(new ShoppingListItemPersistenceEntity(
                updated.getId(), updated.getShoppingListId(), updated.getProductName(), updated.getQuantity(),
                updated.getUnitLabel(), updated.getUnitPrice(), updated.isChecked()));
        return toDomain(saved);
    }

    @Transactional
    public void deleteItem(long ownerId, long listId, long itemId) {
        assertOwner(ownerId, listId);
        var existing = items.findByIdAndShoppingListId(itemId, listId)
                .orElseThrow(() -> new ApplicationErrorException(ApplicationError.notFound("shopping_list_item", Long.toString(itemId))));
        items.delete(existing);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBudget(long ownerId, long listId) {
        assertOwner(ownerId, listId);
        return budgets.findById(listId).map(ShoppingListBudgetPersistenceEntity::getBudgetLimit).orElse(DEFAULT_BUDGET);
    }

    @Transactional
    public BigDecimal updateBudget(long ownerId, long listId, BigDecimal budgetLimit) {
        assertOwner(ownerId, listId);
        if (budgetLimit == null || budgetLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApplicationErrorException(ApplicationError.validationError("budgetLimit", "Budget limit must be positive"));
        }
        BigDecimal scaled = budgetLimit.setScale(2, RoundingMode.HALF_UP);
        budgets.saveAndFlush(new ShoppingListBudgetPersistenceEntity(listId, scaled));
        return scaled;
    }

    private ShoppingListItem toDomain(ShoppingListItemPersistenceEntity e) {
        return new ShoppingListItem(e.getId(), e.getShoppingListId(), e.getProductName(),
                e.getQuantity(), e.getUnitLabel(), e.getUnitPrice(), e.isChecked());
    }
}
