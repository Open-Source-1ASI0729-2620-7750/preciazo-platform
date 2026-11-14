package com.preciazo.platform.shared;

import com.preciazo.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.preciazo.platform.shared.infrastructure.events.DomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainEventPublisherTest {
    static class ExampleAggregate extends AbstractDomainAggregateRoot<ExampleAggregate> {
        void change(Object event) { registerDomainEvent(event); }
    }
    @Test void publishesRegisteredFactsInOrderAndClearsThemWithoutRepeating() {
        List<Object> published = new ArrayList<>();
        ApplicationEventPublisher spring = published::add;
        var publisher = new DomainEventPublisher(spring);
        var aggregate = new ExampleAggregate(); aggregate.change("first"); aggregate.change("second");
        publisher.publishAndClearEvents(aggregate);
        publisher.publishAndClearEvents(aggregate);
        publisher.publishAndClearEvents(null);
        assertThat(published).containsExactly("first", "second");
        assertThat(aggregate.domainEvents()).isEmpty();
    }
    @Test void failedPublicationDoesNotPretendTheEventCollectionWasCleared() {
        ApplicationEventPublisher spring = event -> { throw new IllegalStateException("listener failed"); };
        var aggregate = new ExampleAggregate(); aggregate.change("pending");
        assertThatThrownBy(() -> new DomainEventPublisher(spring).publishAndClearEvents(aggregate)).isInstanceOf(IllegalStateException.class);
        assertThat(aggregate.domainEvents()).containsExactly("pending");
    }
}
