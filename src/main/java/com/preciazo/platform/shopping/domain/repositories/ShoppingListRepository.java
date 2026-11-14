package com.preciazo.platform.shopping.domain.repositories;

import com.preciazo.platform.shopping.domain.model.aggregates.ShoppingList;
import java.util.List;
import java.util.Optional;

public interface ShoppingListRepository {
    Optional<ShoppingList> findById(long id);
    List<ShoppingList> findAllByOwnerId(long ownerId);
    ShoppingList save(ShoppingList list);
}
