package com.preciazo.platform.shopping.application.commandservices;

import com.preciazo.platform.shared.application.result.*;
import com.preciazo.platform.shopping.domain.model.commands.CreateShoppingListCommand;

public interface ShoppingListCommandService {
    Result<Long, ApplicationError> handle(CreateShoppingListCommand command);
}
