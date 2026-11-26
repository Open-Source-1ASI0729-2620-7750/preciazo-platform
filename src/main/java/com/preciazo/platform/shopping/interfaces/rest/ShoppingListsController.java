package com.preciazo.platform.shopping.interfaces.rest;

import com.preciazo.platform.shared.application.result.*;
import com.preciazo.platform.shopping.application.commandservices.ShoppingListCommandService;
import com.preciazo.platform.shopping.application.queryservices.ShoppingListQueryService;
import com.preciazo.platform.shopping.domain.model.queries.*;
import com.preciazo.platform.shopping.interfaces.rest.resources.*;
import com.preciazo.platform.shopping.interfaces.rest.security.CurrentBuyer;
import com.preciazo.platform.shopping.interfaces.rest.transform.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController @RequestMapping("/api/v1/shopping-lists")
@Tag(name = "Shopping Lists", description = "Buyer shopping lists, line items and budget limit management (US08-US10)")
public class ShoppingListsController {
    private final ShoppingListCommandService commands;
    private final ShoppingListQueryService queries;
    public ShoppingListsController(ShoppingListCommandService commands, ShoppingListQueryService queries) { this.commands = commands; this.queries = queries; }

    @PostMapping
    @Operation(summary = "Create a shopping list", description = "Creates a new active shopping list owned by the authenticated buyer.")
    public ResponseEntity<ShoppingListResource> create(@Valid @RequestBody CreateShoppingListResource resource, Principal principal) {
        long ownerId = CurrentBuyer.requireId(principal);
        var result = commands.handle(CreateShoppingListCommandFromResourceAssembler.toCommandFromResource(resource, ownerId));
        if (result instanceof Result.Failure<Long, ApplicationError> failure) throw new ApplicationErrorException(failure.error());
        long id = result.toOptional().orElseThrow();
        var saved = queries.handle(new GetShoppingListByIdQuery(ownerId, id)).orElseThrow();
        return ResponseEntity.created(URI.create("/api/v1/shopping-lists/" + id)).eTag(Long.toString(saved.getVersion()))
            .body(ShoppingListResourceFromEntityAssembler.toResourceFromEntity(saved));
    }
    @GetMapping
    @Operation(summary = "List authenticated buyer's shopping lists", description = "Returns all shopping lists belonging to the authenticated buyer, newest first.")
    public List<ShoppingListResource> ownLists(Principal principal) {
        return queries.handle(new GetSavedShoppingListsQuery(CurrentBuyer.requireId(principal))).stream()
            .map(ShoppingListResourceFromEntityAssembler::toResourceFromEntity).toList();
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get shopping list detail", description = "Returns a single buyer-owned shopping list with its ETag version.")
    public ResponseEntity<ShoppingListResource> detail(@PathVariable long id, Principal principal) {
        var list = queries.handle(new GetShoppingListByIdQuery(CurrentBuyer.requireId(principal), id))
            .orElseThrow(() -> new ApplicationErrorException(ApplicationError.notFound("shopping_list", Long.toString(id))));
        return ResponseEntity.ok().eTag(Long.toString(list.getVersion())).body(ShoppingListResourceFromEntityAssembler.toResourceFromEntity(list));
    }
}
