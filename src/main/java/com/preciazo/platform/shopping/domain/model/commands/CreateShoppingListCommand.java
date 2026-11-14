package com.preciazo.platform.shopping.domain.model.commands;

import org.jspecify.annotations.Nullable;

public record CreateShoppingListCommand(long ownerId, @Nullable String name, String currency) { }
