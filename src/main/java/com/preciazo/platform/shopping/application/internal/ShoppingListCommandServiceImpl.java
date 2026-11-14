package com.preciazo.platform.shopping.application.internal;

import com.preciazo.platform.shared.application.result.*;
import com.preciazo.platform.shopping.application.commandservices.ShoppingListCommandService;
import com.preciazo.platform.shopping.domain.model.aggregates.ShoppingList;
import com.preciazo.platform.shopping.domain.model.commands.CreateShoppingListCommand;
import com.preciazo.platform.shopping.domain.repositories.ShoppingListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.Objects;

@Service
public class ShoppingListCommandServiceImpl implements ShoppingListCommandService {
    private final ShoppingListRepository lists;
    private final Clock clock;
    public ShoppingListCommandServiceImpl(ShoppingListRepository lists, Clock clock) { this.lists = lists; this.clock = clock; }

    @Override @Transactional
    public Result<Long, ApplicationError> handle(CreateShoppingListCommand command) {
        final ShoppingList list;
        try {
            list = ShoppingList.create(command.ownerId(), command.name(), command.currency(), clock);
        } catch (IllegalArgumentException exception) {
            return Result.failure(ApplicationError.validationError("shoppingList", "Invalid list data"));
        }
        ShoppingList saved = lists.save(list);
        return Result.success(Objects.requireNonNull(saved.getId()));
    }
}
