package com.preciazo.platform.shopping.domain.model.aggregates;

import com.preciazo.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.preciazo.platform.shopping.domain.model.events.ShoppingListCreated;
import com.preciazo.platform.shopping.domain.model.valueobjects.ListStatus;
import org.jspecify.annotations.Nullable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/** Shopping list identity and lifecycle, independent of JPA and HTTP. */
public final class ShoppingList extends AbstractDomainAggregateRoot<ShoppingList> {
    private @Nullable Long id;
    private final long ownerId;
    private final String name;
    private final String currency;
    private final ListStatus status;
    private final Instant createdAt;
    private long version;

    private ShoppingList(@Nullable Long id, long ownerId, String name, String currency,
            ListStatus status, Instant createdAt, long version) {
        if (ownerId <= 0 || version < 0 || (id != null && id <= 0)) throw new IllegalArgumentException("Invalid identity or version");
        if (name == null || name.isBlank() || name.length() > 255) throw new IllegalArgumentException("Invalid list name");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Currency is required");
        this.currency = Currency.getInstance(currency.strip().toUpperCase(Locale.ROOT)).getCurrencyCode();
        this.id = id; this.ownerId = ownerId; this.name = name; this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt); this.version = version;
    }

    public static ShoppingList create(long ownerId, @Nullable String name, String currency, Clock clock) {
        Instant now = clock.instant();
        String actualName = name == null || name.isBlank()
            ? "Lista " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(now.atZone(clock.getZone()))
            : name.strip();
        return new ShoppingList(null, ownerId, actualName, currency, ListStatus.ACTIVE, now, 0);
    }

    public static ShoppingList restore(long id, long ownerId, String name, String currency,
            ListStatus status, Instant createdAt, long version) {
        return new ShoppingList(id, ownerId, name, currency, status, createdAt, version);
    }

    /** Repository callback after save/flush; a creation fact always contains the generated id. */
    public void recordPersistence(long generatedId, long persistedVersion) {
        if (generatedId <= 0 || persistedVersion < 0) throw new IllegalArgumentException("Invalid persisted identity");
        if (id == null) {
            id = generatedId;
            registerDomainEvent(new ShoppingListCreated(generatedId, ownerId, createdAt));
        } else if (id != generatedId) {
            throw new IllegalArgumentException("Cannot change the list identity");
        }
        version = persistedVersion;
    }

    public @Nullable Long getId() { return id; }
    public long getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getCurrency() { return currency; }
    public ListStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public long getVersion() { return version; }
}
