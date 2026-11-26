package com.preciazo.platform.merchant.infrastructure.persistence.jpa.repositories;

import com.preciazo.platform.merchant.infrastructure.persistence.jpa.entities.MerchantInventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MerchantInventoryRepository extends JpaRepository<MerchantInventoryEntity, Long> {
    List<MerchantInventoryEntity> findAllByStoreIdOrderByIdAsc(long storeId);
}
