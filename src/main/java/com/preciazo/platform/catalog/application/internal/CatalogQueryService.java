package com.preciazo.platform.catalog.application.internal;

import com.preciazo.platform.catalog.infrastructure.persistence.jpa.entities.CatalogStorePriceEntity;
import com.preciazo.platform.catalog.infrastructure.persistence.jpa.repositories.CatalogStorePriceRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class CatalogQueryService {
    private final CatalogStorePriceRepository repository;

    public CatalogQueryService(CatalogStorePriceRepository repository) {
        this.repository = repository;
    }

    @Order(2)
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedCatalogIfEmpty() {
        if (repository.count() > 0) return;
        List<CatalogStorePriceEntity> seed = List.of(
                new CatalogStorePriceEntity(null, "Arroz Extra 2kg", "Granos", "kg", "Metro", "Supermercado", "Av. Benavides 1944, Miraflores", 0.3, -12.1260, -77.0156, 4.5, false, bd("7.80"), bd("8.90"), "S/ 3.90/kg", "AVAILABLE"),
                new CatalogStorePriceEntity(null, "Arroz Extra 2kg", "Granos", "kg", "Tottus", "Supermercado", "Av. Angamos Este 2650, San Borja", 1.2, -12.1112, -77.0011, 4.3, false, bd("8.20"), bd("8.90"), "S/ 4.10/kg", "AVAILABLE"),
                new CatalogStorePriceEntity(null, "Arroz Extra 2kg", "Granos", "kg", "Wong", "Supermercado", "Av. República de Panamá 5900, Miraflores", 0.7, -12.1235, -77.0205, 4.7, false, bd("8.60"), bd("9.20"), "S/ 4.30/kg", "AVAILABLE"),
                new CatalogStorePriceEntity(null, "Arroz Extra 2kg", "Granos", "kg", "Bodega Don Alberto", "Bodega de Barrio", "Calle San Martín 380, Miraflores", 0.5, -12.1245, -77.0282, 4.8, true, bd("7.60"), bd("8.40"), "S/ 3.80/kg", "AVAILABLE"),

                new CatalogStorePriceEntity(null, "Aceite Vegetal 1L", "Abarrotes", "L", "Metro", "Supermercado", "Av. Benavides 1944, Miraflores", 0.3, -12.1260, -77.0156, 4.5, false, bd("8.90"), bd("11.50"), "S/ 8.90/L", "AVAILABLE"),
                new CatalogStorePriceEntity(null, "Aceite Vegetal 1L", "Abarrotes", "L", "Tottus", "Supermercado", "Av. Angamos Este 2650, San Borja", 1.2, -12.1112, -77.0011, 4.3, false, bd("9.10"), bd("10.50"), "S/ 9.10/L", "AVAILABLE"),
                new CatalogStorePriceEntity(null, "Aceite Vegetal 1L", "Abarrotes", "L", "Wong", "Supermercado", "Av. República de Panamá 5900, Miraflores", 0.7, -12.1235, -77.0205, 4.7, false, bd("9.50"), bd("11.00"), "S/ 9.50/L", "LOW_STOCK"),
                new CatalogStorePriceEntity(null, "Aceite Vegetal 1L", "Abarrotes", "L", "Bodega Don Alberto", "Bodega de Barrio", "Calle San Martín 380, Miraflores", 0.5, -12.1245, -77.0282, 4.8, true, bd("8.70"), bd("10.00"), "S/ 8.70/L", "AVAILABLE"),

                new CatalogStorePriceEntity(null, "Azúcar Rubia 1kg", "Abarrotes", "kg", "Metro", "Supermercado", "Av. Benavides 1944, Miraflores", 0.3, -12.1260, -77.0156, 4.5, false, bd("4.50"), bd("5.00"), "S/ 4.50/kg", "AVAILABLE"),
                new CatalogStorePriceEntity(null, "Azúcar Rubia 1kg", "Abarrotes", "kg", "Tottus", "Supermercado", "Av. Angamos Este 2650, San Borja", 1.2, -12.1112, -77.0011, 4.3, false, bd("4.80"), bd("5.20"), "S/ 4.80/kg", "LOW_STOCK"),
                new CatalogStorePriceEntity(null, "Azúcar Rubia 1kg", "Abarrotes", "kg", "Wong", "Supermercado", "Av. República de Panamá 5900, Miraflores", 0.7, -12.1235, -77.0205, 4.7, false, bd("5.10"), bd("5.50"), "S/ 5.10/kg", "AVAILABLE"),

                new CatalogStorePriceEntity(null, "Leche Evaporada x3", "Lácteos", "lata", "Metro", "Supermercado", "Av. Benavides 1944, Miraflores", 0.3, -12.1260, -77.0156, 4.5, false, bd("9.60"), bd("11.40"), "S/ 3.20/lata", "LOW_STOCK"),
                new CatalogStorePriceEntity(null, "Leche Evaporada x3", "Lácteos", "lata", "Tottus", "Supermercado", "Av. Angamos Este 2650, San Borja", 1.2, -12.1112, -77.0011, 4.3, false, bd("10.20"), bd("11.50"), "S/ 3.40/lata", "AVAILABLE"),
                new CatalogStorePriceEntity(null, "Leche Evaporada x3", "Lácteos", "lata", "Wong", "Supermercado", "Av. República de Panamá 5900, Miraflores", 0.7, -12.1235, -77.0205, 4.7, false, bd("10.80"), bd("12.60"), "S/ 3.60/lata", "AVAILABLE"),

                new CatalogStorePriceEntity(null, "Fideos Spaghetti x2", "Abarrotes", "paq.", "Metro", "Supermercado", "Av. Benavides 1944, Miraflores", 0.3, -12.1260, -77.0156, 4.5, false, bd("5.60"), bd("6.20"), "S/ 2.80/paq.", "AVAILABLE"),
                new CatalogStorePriceEntity(null, "Fideos Spaghetti x2", "Abarrotes", "paq.", "Tottus", "Supermercado", "Av. Angamos Este 2650, San Borja", 1.2, -12.1112, -77.0011, 4.3, false, bd("5.20"), bd("6.00"), "S/ 2.60/paq.", "AVAILABLE"),
                new CatalogStorePriceEntity(null, "Fideos Spaghetti x2", "Abarrotes", "paq.", "Wong", "Supermercado", "Av. República de Panamá 5900, Miraflores", 0.7, -12.1235, -77.0205, 4.7, false, bd("5.80"), bd("6.40"), "S/ 2.90/paq.", "AVAILABLE"),

                new CatalogStorePriceEntity(null, "Detergente 500g", "Limpieza", "unid.", "Metro", "Supermercado", "Av. Benavides 1944, Miraflores", 0.3, -12.1260, -77.0156, 4.5, false, bd("6.90"), bd("8.20"), "S/ 6.90/unid.", "AVAILABLE"),
                new CatalogStorePriceEntity(null, "Detergente 500g", "Limpieza", "unid.", "Tottus", "Supermercado", "Av. Angamos Este 2650, San Borja", 1.2, -12.1112, -77.0011, 4.3, false, bd("7.50"), bd("8.50"), "S/ 7.50/unid.", "AVAILABLE"),
                new CatalogStorePriceEntity(null, "Detergente 500g", "Limpieza", "unid.", "Wong", "Supermercado", "Av. República de Panamá 5900, Miraflores", 0.7, -12.1235, -77.0205, 4.7, false, bd("7.90"), bd("8.90"), "S/ 7.90/unid.", "AVAILABLE"),

                new CatalogStorePriceEntity(null, "Pan de Molde", "Canasta", "unid.", "Metro", "Supermercado", "Av. Benavides 1944, Miraflores", 0.3, -12.1260, -77.0156, 4.5, false, bd("4.50"), bd("6.00"), "S/ 4.50/unid.", "AVAILABLE"),
                new CatalogStorePriceEntity(null, "Pan de Molde", "Canasta", "unid.", "Tottus", "Supermercado", "Av. Angamos Este 2650, San Borja", 1.2, -12.1112, -77.0011, 4.3, false, bd("6.20"), bd("6.80"), "S/ 6.20/unid.", "AVAILABLE"),
                new CatalogStorePriceEntity(null, "Pan de Molde", "Canasta", "unid.", "Wong", "Supermercado", "Av. República de Panamá 5900, Miraflores", 0.7, -12.1235, -77.0205, 4.7, false, bd("0.00"), bd("6.50"), "—", "OUT_OF_STOCK")
        );
        repository.saveAllAndFlush(seed);
    }

    @Transactional(readOnly = true)
    public List<CatalogStorePriceEntity> findProducts(String category, String store, String query) {
        return repository.findAllByOrderByIdAsc().stream()
                .filter(p -> category == null || category.isBlank() || p.getCategory().equalsIgnoreCase(category))
                .filter(p -> store == null || store.isBlank() || p.getStoreName().equalsIgnoreCase(store))
                .filter(p -> query == null || query.isBlank() || p.getProductName().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))
                .toList();
    }

    private static BigDecimal bd(String v) { return new BigDecimal(v); }
}
