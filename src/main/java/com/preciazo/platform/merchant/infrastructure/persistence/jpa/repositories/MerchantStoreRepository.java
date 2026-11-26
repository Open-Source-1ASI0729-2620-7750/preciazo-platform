package com.preciazo.platform.merchant.infrastructure.persistence.jpa.repositories;

import com.preciazo.platform.merchant.infrastructure.persistence.jpa.entities.MerchantStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MerchantStoreRepository extends JpaRepository<MerchantStoreEntity, Long> {
    Optional<MerchantStoreEntity> findByOwnerEmail(String ownerEmail);
}
