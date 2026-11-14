package com.preciazo.platform.shopping.interfaces.rest.resources;

import java.time.Instant;

public record ShoppingListResource(long id, long ownerId, String name, String currency, String status, Instant createdAt, long version) { }
