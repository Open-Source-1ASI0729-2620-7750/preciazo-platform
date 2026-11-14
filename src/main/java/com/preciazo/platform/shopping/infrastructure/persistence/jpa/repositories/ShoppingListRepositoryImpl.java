package com.preciazo.platform.shopping.infrastructure.persistence.jpa.repositories;

import com.preciazo.platform.shared.infrastructure.events.DomainEventPublisher;
import com.preciazo.platform.shopping.domain.model.aggregates.ShoppingList;
import com.preciazo.platform.shopping.domain.repositories.ShoppingListRepository;
import com.preciazo.platform.shopping.infrastructure.persistence.jpa.transform.ShoppingListPersistenceAssembler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository @Transactional(readOnly = true)
public class ShoppingListRepositoryImpl implements ShoppingListRepository {
    private final ShoppingListPersistenceRepository persistence;
    private final DomainEventPublisher events;
    public ShoppingListRepositoryImpl(ShoppingListPersistenceRepository persistence, DomainEventPublisher events) {
        this.persistence = persistence; this.events = events;
    }
    @Override public Optional<ShoppingList> findById(long id) {
        return persistence.findById(id).map(ShoppingListPersistenceAssembler::toDomainFromEntity);
    }
    @Override public List<ShoppingList> findAllByOwnerId(long ownerId) {
        return persistence.findAllByOwnerIdOrderByCreatedAtDescIdDesc(ownerId).stream()
            .map(ShoppingListPersistenceAssembler::toDomainFromEntity).toList();
    }
    @Override @Transactional public ShoppingList save(ShoppingList list) {
        var saved = persistence.saveAndFlush(ShoppingListPersistenceAssembler.toEntityFromDomain(list));
        list.recordPersistence(saved.getId(), saved.getVersion());
        events.publishAndClearEvents(list);
        return list;
    }
}
