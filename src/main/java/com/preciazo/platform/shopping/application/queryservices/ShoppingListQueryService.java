package com.preciazo.platform.shopping.application.queryservices;

import com.preciazo.platform.shopping.domain.model.aggregates.ShoppingList;
import com.preciazo.platform.shopping.domain.model.queries.*;
import java.util.List;
import java.util.Optional;

public interface ShoppingListQueryService {
    List<ShoppingList> handle(GetSavedShoppingListsQuery query);
    Optional<ShoppingList> handle(GetShoppingListByIdQuery query);
}
