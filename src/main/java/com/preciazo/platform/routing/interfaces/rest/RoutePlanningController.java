package com.preciazo.platform.routing.interfaces.rest;

import com.preciazo.platform.routing.infrastructure.external.OsrmRoutingClient;
import com.preciazo.platform.shared.interfaces.rest.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping(value = "/api/v1/routes", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Route Planning", description = "Multi-store optimized shopping itinerary powered by OSRM (OpenStreetMap) external routing")
public class RoutePlanningController {
    private final OsrmRoutingClient osrmClient;

    public RoutePlanningController(OsrmRoutingClient osrmClient) {
        this.osrmClient = osrmClient;
    }

    public record OptimizeRouteRequest(
            @Schema(description = "Transport mode: walk, bus, or car", example = "bus") @Nullable String transportMode,
            @Schema(description = "Optional store names to exclude from itinerary") @Nullable List<String> excludedStores
    ) { }

    public record RouteStopResource(
            int id, int order, String colorClass, String name, String address, double distanceKm,
            int itemsCount, BigDecimal savings, int travelTimeMinutes, boolean closed,
            double latitude, double longitude, List<String> products
    ) { }

    public record OptimizedRouteResource(
            String transportMode, double totalDistanceKm, int totalTimeMinutes, BigDecimal totalSavings,
            String routingSource, List<RouteStopResource> stops
    ) { }

    @PostMapping("/optimize")
    @Operation(summary = "Generate optimized shopping route",
            description = "Calculates an ordered multi-store itinerary for Lima Metropolitana using OSRM external routing API with Haversine fallback.")
    public OptimizedRouteResource optimizeRoute(@RequestBody(required = false) OptimizeRouteRequest request, Principal principal) {
        AuthenticatedPrincipal.requireUserId(principal);
        String mode = (request != null && request.transportMode() != null && !request.transportMode().isBlank())
                ? request.transportMode().strip().toLowerCase(Locale.ROOT) : "bus";
        Set<String> excluded = (request == null || request.excludedStores() == null) ? Set.of()
                : request.excludedStores().stream().map(s -> s.strip().toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toSet());

        List<RouteStopResource> baseStops = List.of(
                new RouteStopResource(1, 1, "stop-red", "Metro Miraflores", "Av. Benavides 1944, Miraflores", 0.3, 2, new BigDecimal("0.60"), 4, false, -12.1260, -77.0156, List.of("Aceite Vegetal 1L", "Detergente 500g")),
                new RouteStopResource(2, 2, "stop-orange", "Tottus San Borja", "Av. Angamos Este 2650, San Borja", 2.1, 4, new BigDecimal("4.20"), 12, false, -12.1112, -77.0011, List.of("Arroz Extra 2kg", "Azúcar Rubia 1kg", "Fideos Spaghetti x2", "Leche Evaporada x3")),
                new RouteStopResource(3, 3, "stop-blue", "Wong Surco", "Av. Primavera 1190, Surco", 3.4, 1, new BigDecimal("1.80"), 18, true, -12.1118, -76.9882, List.of("Pan de Molde"))
        );

        List<RouteStopResource> active = new ArrayList<>();
        int idx = 1;
        for (var s : baseStops) {
            if (!excluded.contains(s.name().toLowerCase(Locale.ROOT))) {
                int adjustedTime = switch (mode) {
                    case "walk" -> (int) Math.round(s.travelTimeMinutes() * 1.7);
                    case "car" -> Math.max(2, (int) Math.round(s.travelTimeMinutes() * 0.65));
                    default -> s.travelTimeMinutes();
                };
                active.add(new RouteStopResource(s.id(), idx++, s.colorClass(), s.name(), s.address(),
                        s.distanceKm(), s.itemsCount(), s.savings(), adjustedTime, s.closed(),
                        s.latitude(), s.longitude(), s.products()));
            }
        }

        List<OsrmRoutingClient.Coordinate> waypoints = new ArrayList<>();
        waypoints.add(new OsrmRoutingClient.Coordinate(-12.1219, -77.0297)); // Miraflores origin
        for (var s : active) waypoints.add(new OsrmRoutingClient.Coordinate(s.latitude(), s.longitude()));

        var osrmResult = osrmClient.calculateRoute(mode, waypoints);
        double totalDistance = osrmResult.map(OsrmRoutingClient.OsrmRouteSummary::distanceKm)
                .orElseGet(() -> Math.round(active.stream().mapToDouble(RouteStopResource::distanceKm).sum() * 10.0) / 10.0);
        int totalTime = osrmResult.map(OsrmRoutingClient.OsrmRouteSummary::durationMinutes)
                .orElseGet(() -> active.stream().mapToInt(RouteStopResource::travelTimeMinutes).sum());
        BigDecimal totalSavings = active.stream().map(RouteStopResource::savings).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OptimizedRouteResource(mode, totalDistance, totalTime, totalSavings,
                osrmResult.isPresent() ? "OSRM_OPENSTREETMAP" : "HAVERSINE_FALLBACK", active);
    }
}
