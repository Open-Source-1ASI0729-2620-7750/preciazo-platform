package com.preciazo.platform.shopping.interfaces.rest.transform;

import com.preciazo.platform.shopping.domain.model.commands.CreateShoppingListCommand;
import com.preciazo.platform.shopping.interfaces.rest.resources.CreateShoppingListResource;

public final class CreateShoppingListCommandFromResourceAssembler {
    private CreateShoppingListCommandFromResourceAssembler() { }
    public static CreateShoppingListCommand toCommandFromResource(CreateShoppingListResource resource, long ownerId) {
        return new CreateShoppingListCommand(ownerId, resource.name(), resource.currency());
    }
}
