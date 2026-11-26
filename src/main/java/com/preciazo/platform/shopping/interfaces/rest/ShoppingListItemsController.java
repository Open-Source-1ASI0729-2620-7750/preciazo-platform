package com.preciazo.platform.shopping.interfaces.rest;

import com.preciazo.platform.shopping.application.internal.ShoppingListItemsService;
import com.preciazo.platform.shopping.domain.model.entities.ShoppingListItem;
import com.preciazo.platform.shopping.interfaces.rest.security.CurrentBuyer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/v1/shopping-lists/{listId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Shopping Lists", description = "Buyer shopping lists, line items and budget limit management (US08-US10)")
public class ShoppingListItemsController {
    private final ShoppingListItemsService itemsService;

    public ShoppingListItemsController(ShoppingListItemsService itemsService) {
        this.itemsService = itemsService;
    }

    public record AddItemResource(
            @Schema(example = "Arroz Extra 2kg") @NotBlank String productName,
            @Schema(example = "2") @Min(1) int quantity,
            @Schema(example = "kg") @Nullable String unitLabel,
            @Schema(example = "3.90") @NotNull @DecimalMin("0.00") BigDecimal unitPrice
    ) { }

    public record UpdateItemResource(
            @Nullable String productName,
            @Nullable Integer quantity,
            @Nullable BigDecimal unitPrice,
            @Schema(example = "true") @Nullable Boolean checked
    ) { }

    public record ShoppingListItemResource(
            long id, long shoppingListId, String productName, int quantity, String unitLabel,
            BigDecimal unitPrice, BigDecimal subtotal, boolean checked
    ) { }

    public record BudgetResource(@Schema(example = "150.00") @NotNull @DecimalMin("0.01") BigDecimal budgetLimit) { }

    @GetMapping("/items")
    @Operation(summary = "List items in a shopping list", description = "Returns all items in the specified buyer-owned shopping list.")
    public List<ShoppingListItemResource> listItems(@PathVariable long listId, Principal principal) {
        return itemsService.listItems(CurrentBuyer.requireId(principal), listId).stream().map(this::toResource).toList();
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to a shopping list", description = "Adds a product line item with quantity and unit price to the buyer's shopping list.")
    public ResponseEntity<ShoppingListItemResource> addItem(@PathVariable long listId,
                                                            @Valid @RequestBody AddItemResource resource,
                                                            Principal principal) {
        var created = itemsService.addItem(CurrentBuyer.requireId(principal), listId,
                resource.productName(), resource.quantity(), resource.unitLabel(), resource.unitPrice());
        long id = Objects.requireNonNull(created.getId());
        return ResponseEntity.created(URI.create("/api/v1/shopping-lists/" + listId + "/items/" + id))
                .body(toResource(created));
    }

    @PatchMapping("/items/{itemId}")
    @Operation(summary = "Update item or toggle purchased status", description = "Updates item quantity, price, name or checked status in Shopping Mode.")
    public ShoppingListItemResource updateItem(@PathVariable long listId, @PathVariable long itemId,
                                               @RequestBody UpdateItemResource resource, Principal principal) {
        return toResource(itemsService.updateItem(CurrentBuyer.requireId(principal), listId, itemId,
                resource.productName(), resource.quantity(), resource.unitPrice(), resource.checked()));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from a shopping list", description = "Deletes a product item from the buyer's shopping list.")
    public ResponseEntity<Void> deleteItem(@PathVariable long listId, @PathVariable long itemId, Principal principal) {
        itemsService.deleteItem(CurrentBuyer.requireId(principal), listId, itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/budget")
    @Operation(summary = "Get shopping list budget limit", description = "Returns the configured maximum budget limit for the shopping list.")
    public BudgetResource getBudget(@PathVariable long listId, Principal principal) {
        return new BudgetResource(itemsService.getBudget(CurrentBuyer.requireId(principal), listId));
    }

    @PatchMapping("/budget")
    @Operation(summary = "Update shopping list budget limit", description = "Sets a new maximum budget limit for the buyer's shopping list (US10).")
    public BudgetResource updateBudget(@PathVariable long listId, @Valid @RequestBody BudgetResource resource, Principal principal) {
        return new BudgetResource(itemsService.updateBudget(CurrentBuyer.requireId(principal), listId, resource.budgetLimit()));
    }

    private ShoppingListItemResource toResource(ShoppingListItem item) {
        return new ShoppingListItemResource(Objects.requireNonNull(item.getId()), item.getShoppingListId(),
                item.getProductName(), item.getQuantity(), item.getUnitLabel(), item.getUnitPrice(),
                item.getSubtotal(), item.isChecked());
    }
}
