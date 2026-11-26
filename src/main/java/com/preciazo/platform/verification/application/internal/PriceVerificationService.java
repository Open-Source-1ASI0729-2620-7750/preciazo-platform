package com.preciazo.platform.verification.application.internal;

import com.preciazo.platform.shared.application.result.ApplicationError;
import com.preciazo.platform.shared.application.result.ApplicationErrorException;
import com.preciazo.platform.verification.infrastructure.persistence.jpa.entities.PriceVerificationEntity;
import com.preciazo.platform.verification.infrastructure.persistence.jpa.repositories.PriceVerificationRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PriceVerificationService {
    private final PriceVerificationRepository repository;

    public PriceVerificationService(PriceVerificationRepository repository) {
        this.repository = repository;
    }

    @Order(4)
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedVerificationsIfEmpty() {
        if (repository.count() > 0) return;
        Instant now = Instant.now();
        repository.saveAllAndFlush(List.of(
                new PriceVerificationEntity(null, "Metro Miraflores", "Aceite Vegetal 1L", new BigDecimal("8.90"), new BigDecimal("8.50"), 8, 0, "PENDING", 1L, now.minus(15, ChronoUnit.MINUTES)),
                new PriceVerificationEntity(null, "Tottus San Borja", "Arroz Extra 2kg", new BigDecimal("8.20"), new BigDecimal("7.60"), 5, 1, "PENDING", 1L, now.minus(42, ChronoUnit.MINUTES)),
                new PriceVerificationEntity(null, "Wong Surco", "Leche Evaporada x3", new BigDecimal("10.80"), new BigDecimal("10.50"), 3, 0, "PENDING", 1L, now.minus(60, ChronoUnit.MINUTES))
        ));
    }

    @Transactional(readOnly = true)
    public List<PriceVerificationEntity> listAll() {
        return repository.findAllByOrderByCreatedAtDescIdDesc();
    }

    @Transactional
    public PriceVerificationEntity createReport(long userId, String storeName, String productName,
                                                BigDecimal publishedPrice, BigDecimal reportedPrice) {
        BigDecimal pub = (publishedPrice != null && publishedPrice.compareTo(BigDecimal.ZERO) > 0)
                ? publishedPrice.setScale(2, RoundingMode.HALF_UP)
                : reportedPrice.multiply(new BigDecimal("1.05")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal rep = reportedPrice.setScale(2, RoundingMode.HALF_UP);
        return repository.saveAndFlush(new PriceVerificationEntity(
                null, storeName.strip(), productName.strip(), pub, rep, 1, 0, "PENDING", userId, Instant.now()));
    }

    @Transactional
    public PriceVerificationEntity vote(long reportId, boolean confirm) {
        var existing = repository.findById(reportId)
                .orElseThrow(() -> new ApplicationErrorException(ApplicationError.notFound("price_verification", Long.toString(reportId))));
        return repository.saveAndFlush(new PriceVerificationEntity(
                existing.getId(), existing.getStoreName(), existing.getProductName(),
                existing.getPublishedPrice(), existing.getReportedPrice(),
                confirm ? existing.getConfirmationsCount() + 1 : existing.getConfirmationsCount(),
                confirm ? existing.getRejectionsCount() : existing.getRejectionsCount() + 1,
                confirm ? "CONFIRMED" : "REJECTED",
                existing.getReportedByUserId(), existing.getCreatedAt()));
    }
}
