package com.preciazo.platform.shopping.application.internal;

import com.preciazo.platform.shared.application.result.*;
import com.preciazo.platform.shopping.application.queryservices.ShoppingListQueryService;
import com.preciazo.platform.shopping.domain.model.aggregates.ShoppingList;
import com.preciazo.platform.shopping.domain.model.queries.*;
import com.preciazo.platform.shopping.domain.repositories.ShoppingListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service @Transactional(readOnly = true)
public class ShoppingListQueryServiceImpl implements ShoppingListQueryService {
    private final ShoppingListRepository lists;
    public ShoppingListQueryServiceImpl(ShoppingListRepository lists) { this.lists = lists; }
    @Override public List<ShoppingList> handle(GetSavedShoppingListsQuery query) { return lists.findAllByOwnerId(query.ownerId()); }
    @Override public Optional<ShoppingList> handle(GetShoppingListByIdQuery query) {
        if (query.listId() <= 0) throw new ApplicationErrorException(new ApplicationError("VALIDATION_ERROR", "Invalid list id"));
        var list = lists.findById(query.listId());
        if (list.isPresent() && list.get().getOwnerId() != query.ownerId()) {
            throw new ApplicationErrorException(new ApplicationError("SHOPPING_FORBIDDEN", "List belongs to another buyer"));
        }
        return list;
    }
}
