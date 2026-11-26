package com.preciazo.platform.shopping;

import com.jayway.jsonpath.JsonPath;
import com.preciazo.platform.iam.application.commandservices.UserCommandService;
import com.preciazo.platform.iam.domain.model.commands.SignInCommand;
import com.preciazo.platform.iam.domain.model.commands.SignUpCommand;
import com.preciazo.platform.shopping.domain.model.events.ShoppingListCreated;
import com.preciazo.platform.shopping.domain.repositories.ShoppingListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import javax.sql.DataSource;
import java.time.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:shopping-contract;MODE=MySQL;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ShoppingListApiTest.Fixtures.class)
class ShoppingListApiTest {
    private static final AtomicLong SEQ = new AtomicLong(1);
    @Autowired MockMvc mvc;
    @Autowired ShoppingListRepository lists;
    @Autowired UserCommandService users;
    @Autowired Events events;
    @Autowired DataSource dataSource;
    private long owner;
    private String ownerToken;
    private String otherBuyerToken;

    @DynamicPropertySource static void mysqlWhenExplicitlyConfigured(DynamicPropertyRegistry properties) {
        String url = System.getenv("PRECIAZO_MYSQL_TEST_URL");
        if (url != null && !url.isBlank()) {
            // This suite uses create-drop. Never accept an operational/team schema.
            if (!url.matches("jdbc:mysql://[^/]+/preciazo_shopping_test(?:\\?.*)?")) {
                throw new IllegalStateException("MySQL tests require the dedicated preciazo_shopping_test schema");
            }
            properties.add("spring.datasource.url", () -> url);
            properties.add("spring.datasource.username", () -> System.getenv("PRECIAZO_MYSQL_TEST_USER"));
            properties.add("spring.datasource.password", () -> System.getenv("PRECIAZO_MYSQL_TEST_PASSWORD"));
            properties.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        }
    }

    private UserCommandService.AuthenticatedUser registerBuyer() {
        long n = SEQ.incrementAndGet();
        String email = "buyer" + n + "@test.preciazo.pe";
        users.handle(new SignUpCommand(email, "Buyer " + n, "TestPass2026!", List.of("ROLE_BUYER")));
        return users.handle(new SignInCommand(email, "TestPass2026!")).toOptional().orElseThrow();
    }

    @BeforeEach void newOwner() {
        var mainBuyer = registerBuyer();
        owner = Objects.requireNonNull(mainBuyer.user().getId());
        ownerToken = mainBuyer.token();
        otherBuyerToken = registerBuyer().token();
        events.recorded.clear();
    }

    private MvcResult create(String token, String payload) throws Exception {
        return mvc.perform(post("/api/v1/shopping-lists").header("Authorization", "Bearer " + token)
            .contentType("application/json").content(payload))
            .andExpect(status().isCreated()).andReturn();
    }
    private long id(MvcResult result) throws Exception {
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }
    @Test void rejectsAnonymousRequestsWithoutCreatingAnOwnerOrList() throws Exception {
        mvc.perform(post("/api/v1/shopping-lists").contentType("application/json").content("{\"currency\":\"PEN\"}"))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        mvc.perform(get("/api/v1/shopping-lists")).andExpect(status().isUnauthorized());
        assertThat(events.recorded).isEmpty();
    }
    @Test void createsThenRetrievesAnOwnedListWithGeneratedIdAndSingleEvent() throws Exception {
        var created = create(ownerToken, "{\"currency\":\"PEN\"}"); long listId = id(created);
        assertThat(created.getResponse().getHeader("Location")).isEqualTo("/api/v1/shopping-lists/" + listId);
        assertThat(created.getResponse().getHeader("ETag")).isEqualTo("\"0\"");
        mvc.perform(get("/api/v1/shopping-lists/" + listId).header("Authorization", "Bearer " + ownerToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.ownerId").value(owner))
            .andExpect(jsonPath("$.name").value("Lista 2026-11-13 23:00:00"))
            .andExpect(jsonPath("$.status").value("ACTIVE")).andExpect(jsonPath("$.currency").value("PEN"));
        assertThat(lists.findById(listId).orElseThrow().getOwnerId()).isEqualTo(owner);
        assertThat(events.recorded).containsExactly(new ShoppingListCreated(listId, owner, Instant.parse("2026-11-14T04:00:00Z")));
    }
    @Test void returnsOnlyOwnListsInStableNewestFirstOrder() throws Exception {
        long first = id(create(ownerToken, "{\"name\":\"First\",\"currency\":\"PEN\"}"));
        long second = id(create(ownerToken, "{\"name\":\"Second\",\"currency\":\"PEN\"}"));
        create(otherBuyerToken, "{\"name\":\"Other buyer\",\"currency\":\"PEN\"}");
        mvc.perform(get("/api/v1/shopping-lists").header("Authorization", "Bearer " + ownerToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(second)).andExpect(jsonPath("$[1].id").value(first));
    }
    @Test void forbidsAnotherBuyersDetailAndDistinguishesMissingResource() throws Exception {
        long listId = id(create(ownerToken, "{\"name\":\"Private\",\"currency\":\"PEN\"}"));
        mvc.perform(get("/api/v1/shopping-lists/" + listId).header("Authorization", "Bearer " + otherBuyerToken).header("Accept-Language", "es"))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("SHOPPING_FORBIDDEN"));
        mvc.perform(get("/api/v1/shopping-lists/999999999").header("Authorization", "Bearer " + ownerToken))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("SHOPPING_LIST_NOT_FOUND"));
    }
    @Test void invalidCurrencyAndOversizedNameDoNotPersistOrPublish() throws Exception {
        for (String payload : List.of("{\"currency\":\"XYZ\"}", "{\"currency\":\"PEN\",\"name\":\"" + "x".repeat(256) + "\"}")) {
            mvc.perform(post("/api/v1/shopping-lists").header("Authorization", "Bearer " + ownerToken).contentType("application/json").content(payload))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
        assertThat(lists.findAllByOwnerId(owner)).isEmpty(); assertThat(events.recorded).isEmpty();
    }
    @Test void neverUsesACallerSuppliedOwnerAsIdentity() throws Exception {
        mvc.perform(post("/api/v1/shopping-lists").header("Authorization", "Bearer " + ownerToken).contentType("application/json")
            .content("{\"name\":\"Spoof\",\"currency\":\"PEN\",\"ownerId\":999}"))
            .andExpect(status().isBadRequest());
        assertThat(lists.findAllByOwnerId(999)).isEmpty();
    }
    @Test void usesTheRequestedDatabaseEngine() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName())
                .isEqualTo(System.getenv("PRECIAZO_MYSQL_TEST_URL") == null ? "H2" : "MySQL");
        }
    }
    static class Events {
        final List<ShoppingListCreated> recorded = new CopyOnWriteArrayList<>();
        @EventListener void on(ShoppingListCreated event) { recorded.add(event); }
    }
    @TestConfiguration static class Fixtures {
        @Bean @Primary Clock testClock() { return Clock.fixed(Instant.parse("2026-11-14T04:00:00Z"), ZoneId.of("America/Lima")); }
        @Bean Events events() { return new Events(); }
    }
}
