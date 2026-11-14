package com.preciazo.platform.shopping.interfaces.rest.transform;

import com.preciazo.platform.shopping.domain.model.aggregates.ShoppingList;
import com.preciazo.platform.shopping.interfaces.rest.resources.ShoppingListResource;
import java.util.Objects;

public final class ShoppingListResourceFromEntityAssembler {
    private ShoppingListResourceFromEntityAssembler() { }
    public static ShoppingListResource toResourceFromEntity(ShoppingList list) {
        return new ShoppingListResource(Objects.requireNonNull(list.getId()), list.getOwnerId(), list.getName(),
            list.getCurrency(), list.getStatus().name(), list.getCreatedAt(), list.getVersion());
    }
}
