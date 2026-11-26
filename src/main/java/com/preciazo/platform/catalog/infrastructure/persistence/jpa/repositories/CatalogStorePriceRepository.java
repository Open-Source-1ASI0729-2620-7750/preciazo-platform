package com.preciazo.platform.catalog.infrastructure.persistence.jpa.repositories;

import com.preciazo.platform.catalog.infrastructure.persistence.jpa.entities.CatalogStorePriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CatalogStorePriceRepository extends JpaRepository<CatalogStorePriceEntity, Long> {
    List<CatalogStorePriceEntity> findAllByOrderByIdAsc();
}
