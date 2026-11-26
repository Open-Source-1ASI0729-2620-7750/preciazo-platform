package com.preciazo.platform.shopping.infrastructure.persistence.jpa.repositories;

import com.preciazo.platform.shopping.infrastructure.persistence.jpa.entities.ShoppingListItemPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ShoppingListItemPersistenceRepository extends JpaRepository<ShoppingListItemPersistenceEntity, Long> {
    List<ShoppingListItemPersistenceEntity> findAllByShoppingListIdOrderByIdAsc(long shoppingListId);
    Optional<ShoppingListItemPersistenceEntity> findByIdAndShoppingListId(long id, long shoppingListId);
}
