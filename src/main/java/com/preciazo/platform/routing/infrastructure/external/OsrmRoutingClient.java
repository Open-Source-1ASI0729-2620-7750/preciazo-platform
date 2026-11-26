package com.preciazo.platform.routing.infrastructure.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Third-party integration with OSRM (OpenStreetMap Routing Machine) for real road distance and duration. */
@Component
public class OsrmRoutingClient {
    private static final Logger LOG = LoggerFactory.getLogger(OsrmRoutingClient.class);
    private static final Pattern DISTANCE_PATTERN = Pattern.compile("\"distance\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
    private static final Pattern DURATION_PATTERN = Pattern.compile("\"duration\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");

    public record Coordinate(double latitude, double longitude) { }
    public record OsrmRouteSummary(double distanceKm, int durationMinutes) { }

    private final HttpClient httpClient;
    private final String baseUrl;
    private final boolean enabled;

    public OsrmRoutingClient(
            @Value("${preciazo.routing.osrm.base-url:https://router.project-osrm.org}") String baseUrl,
            @Value("${preciazo.routing.osrm.enabled:true}") boolean enabled) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.enabled = enabled;
    }

    public Optional<OsrmRouteSummary> calculateRoute(String transportMode, List<Coordinate> waypoints) {
        if (!enabled || waypoints.size() < 2) return Optional.empty();
        String profile = "walk".equalsIgnoreCase(transportMode) ? "foot" : "driving";
        StringBuilder coords = new StringBuilder();
        for (int i = 0; i < waypoints.size(); i++) {
            if (i > 0) coords.append(';');
            coords.append(String.format(Locale.ROOT, "%.6f,%.6f", waypoints.get(i).longitude(), waypoints.get(i).latitude()));
        }
        String url = baseUrl + "/route/v1/" + profile + "/" + coords + "?overview=false";
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMillis(2500))
                    .header("User-Agent", "PreciazoPlatform/1.0")
                    .GET().build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return Optional.empty();
            Matcher distMatcher = DISTANCE_PATTERN.matcher(resp.body());
            Matcher durMatcher = DURATION_PATTERN.matcher(resp.body());
            if (distMatcher.find() && durMatcher.find()) {
                double distanceKm = Double.parseDouble(distMatcher.group(1)) / 1000.0;
                double rawMinutes = Double.parseDouble(durMatcher.group(1)) / 60.0;
                if ("bus".equalsIgnoreCase(transportMode)) rawMinutes *= 1.45;
                return Optional.of(new OsrmRouteSummary(Math.round(distanceKm * 10.0) / 10.0, Math.max(1, (int) Math.round(rawMinutes))));
            }
        } catch (Exception ex) {
            LOG.debug("OSRM routing unavailable, falling back to Haversine calculation: {}", ex.getClass().getSimpleName());
        }
        return Optional.empty();
    }
}
