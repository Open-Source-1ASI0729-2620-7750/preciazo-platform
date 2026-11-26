package com.preciazo.platform.shopping.infrastructure.persistence.jpa.repositories;

import com.preciazo.platform.shopping.infrastructure.persistence.jpa.entities.ShoppingListBudgetPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingListBudgetPersistenceRepository extends JpaRepository<ShoppingListBudgetPersistenceEntity, Long> { }
