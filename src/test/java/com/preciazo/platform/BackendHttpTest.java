package com.preciazo.platform;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(BackendHttpTest.TestOnlyController.class)
class BackendHttpTest {
    @LocalServerPort int port;
    private final HttpClient http = HttpClient.newHttpClient();
    private HttpResponse<String> get(String path) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test void startsJpaAndExposesHealthAndDocumentedApi() throws Exception {
        var health = get("/actuator/health"); assertThat(health.statusCode()).isEqualTo(200); assertThat(health.body()).contains("UP");
        var docs = get("/v3/api-docs"); assertThat(docs.statusCode()).isEqualTo(200); assertThat(docs.body()).contains("Preciazo REST API");
    }
    @Test void returnsSanitizedProblemDetailForUnexpectedFailure() throws Exception {
        var response = get("/_test/failure");
        assertThat(response.statusCode()).isEqualTo(500);
        assertThat(response.headers().firstValue("Content-Type").orElse("")).contains("application/problem+json");
        assertThat(response.body()).contains("UNEXPECTED_ERROR").doesNotContain("private-database-password");
    }
    @Test void missingRouteHasAProblemResponse() throws Exception {
        var response = get("/api/v1/does-not-exist"); assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.headers().firstValue("Content-Type").orElse("")).contains("application/problem+json");
    }
    @Test void malformedJsonAndInvalidInputAre400WithoutEchoingValues() throws Exception {
        for (String body : new String[]{"{broken-secret", "{}"}) {
            var response = http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/_test/validation"))
                .header("Content-Type", "application/json").header("Accept-Language", "es")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(400);
            assertThat(response.body()).contains("VALIDATION_ERROR").doesNotContain("broken-secret");
        }
    }
    @RestController static class TestOnlyController {
        @GetMapping("/_test/failure") String fail() { throw new IllegalStateException("private-database-password"); }
        @PostMapping("/_test/validation") String validate(@Valid @RequestBody Input input) { return input.name(); }
    }
    record Input(@NotBlank String name) { }
}
