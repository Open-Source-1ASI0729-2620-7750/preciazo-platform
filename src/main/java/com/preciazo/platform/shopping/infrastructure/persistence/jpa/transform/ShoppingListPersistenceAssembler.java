package com.preciazo.platform.shopping.infrastructure.persistence.jpa.transform;

import com.preciazo.platform.shopping.domain.model.aggregates.ShoppingList;
import com.preciazo.platform.shopping.infrastructure.persistence.jpa.entities.ShoppingListPersistenceEntity;

public final class ShoppingListPersistenceAssembler {
    private ShoppingListPersistenceAssembler() { }
    public static ShoppingListPersistenceEntity toEntityFromDomain(ShoppingList list) {
        return new ShoppingListPersistenceEntity(list.getId(), list.getOwnerId(), list.getName(), list.getCurrency(),
            list.getStatus(), list.getCreatedAt(), list.getId() == null ? null : list.getVersion());
    }
    public static ShoppingList toDomainFromEntity(ShoppingListPersistenceEntity entity) {
        return ShoppingList.restore(entity.getId(), entity.getOwnerId(), entity.getName(), entity.getCurrency(),
            entity.getStatus(), entity.getCreatedAt(), entity.getVersion());
    }
}
