package com.preciazo.platform.shopping.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record CreateShoppingListResource(@Nullable @Size(max = 255) String name, @NotBlank String currency) { }
