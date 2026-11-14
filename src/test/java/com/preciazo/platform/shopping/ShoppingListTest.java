package com.preciazo.platform.shopping;

import com.preciazo.platform.shopping.domain.model.aggregates.ShoppingList;
import com.preciazo.platform.shopping.domain.model.events.ShoppingListCreated;
import com.preciazo.platform.shopping.domain.model.valueobjects.ListStatus;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.assertj.core.api.Assertions.*;

class ShoppingListTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-14T04:00:00Z"), ZoneId.of("America/Lima"));
    @Test void createsActiveListWithServerDateWhenNameIsMissing() {
        var list = ShoppingList.create(7, "  ", " pen ", clock);
        assertThat(list.getId()).isNull();
        assertThat(list.getName()).isEqualTo("Lista 2026-11-13 23:00:00");
        assertThat(list.getCurrency()).isEqualTo("PEN");
        assertThat(list.getStatus()).isEqualTo(ListStatus.ACTIVE);
        assertThat(list.getOwnerId()).isEqualTo(7);
        assertThat(list.getCreatedAt()).isEqualTo(clock.instant());
        assertThat(list.getVersion()).isZero();
    }
    @Test void rejectsInvalidInputBeforePersistence() {
        assertThatIllegalArgumentException().isThrownBy(() -> ShoppingList.create(0, "List", "PEN", clock));
        assertThatIllegalArgumentException().isThrownBy(() -> ShoppingList.create(7, "List", "XYZ", clock));
        assertThatIllegalArgumentException().isThrownBy(() -> ShoppingList.create(7, "x".repeat(256), "PEN", clock));
    }
    @Test void emitsCreationOnlyAfterReceivingARealIdentityAndReadsDoNotReemitIt() {
        var list = ShoppingList.create(7, " Weekly ", "PEN", clock);
        assertThat(list.domainEvents()).isEmpty();
        list.recordPersistence(42L, 0L);
        assertThat(list.domainEvents()).containsExactly(new ShoppingListCreated(42L, 7L, clock.instant()));
        list.clearDomainEvents(); list.recordPersistence(42L, 0L);
        assertThat(list.domainEvents()).isEmpty();
        var loaded = ShoppingList.restore(42L, 7, "Weekly", "PEN", ListStatus.ACTIVE, clock.instant(), 0);
        assertThat(loaded.domainEvents()).isEmpty();
        assertThat(loaded.getName()).isEqualTo("Weekly");
    }
}
