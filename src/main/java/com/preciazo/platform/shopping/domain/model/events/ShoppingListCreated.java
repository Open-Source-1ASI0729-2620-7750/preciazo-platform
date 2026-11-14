package com.preciazo.platform.shopping.domain.model.events;

import java.time.Instant;

public record ShoppingListCreated(long shoppingListId, long ownerId, Instant occurredAt) { }
