package com.preciazo.platform.merchant.infrastructure.persistence.jpa.repositories;

import com.preciazo.platform.merchant.infrastructure.persistence.jpa.entities.MerchantPromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MerchantPromotionRepository extends JpaRepository<MerchantPromotionEntity, Long> {
    List<MerchantPromotionEntity> findAllByStoreIdOrderByIdDesc(long storeId);
}
