package com.preciazo.platform.verification.infrastructure.persistence.jpa.repositories;

import com.preciazo.platform.verification.infrastructure.persistence.jpa.entities.PriceVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PriceVerificationRepository extends JpaRepository<PriceVerificationEntity, Long> {
    List<PriceVerificationEntity> findAllByOrderByCreatedAtDescIdDesc();
}
