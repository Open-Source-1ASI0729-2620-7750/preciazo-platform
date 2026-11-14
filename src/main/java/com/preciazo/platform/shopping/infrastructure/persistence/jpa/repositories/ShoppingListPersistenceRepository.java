package com.preciazo.platform.shopping.infrastructure.persistence.jpa.repositories;

import com.preciazo.platform.shopping.infrastructure.persistence.jpa.entities.ShoppingListPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShoppingListPersistenceRepository extends JpaRepository<ShoppingListPersistenceEntity, Long> {
    List<ShoppingListPersistenceEntity> findAllByOwnerIdOrderByCreatedAtDescIdDesc(long ownerId);
}
