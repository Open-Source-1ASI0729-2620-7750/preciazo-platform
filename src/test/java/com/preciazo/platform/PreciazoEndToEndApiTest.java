package com.preciazo.platform;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:preciazo-e2e;MODE=MySQL;DB_CLOSE_DELAY=-1"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PreciazoEndToEndApiTest {
    @Autowired MockMvc mvc;

    private String signIn(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    @Test
    void iamSeedsRolesAndDemoAccountsAndIssuesWorkingJwt() throws Exception {
        mvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        // Invalid credentials -> 401
        mvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"carlos.mendoza@preciazo.pe\",\"password\":\"WrongPass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));

        // Sign-up new user -> 201
        mvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nuevo.comprador@preciazo.pe\",\"fullName\":\"Nuevo Comprador\",\"password\":\"Secret123!\",\"roles\":[\"ROLE_BUYER\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("nuevo.comprador@preciazo.pe"));

        // Sign-in seeded buyer -> 200 + JWT works on /api/v1/users/me
        String buyerToken = signIn("carlos.mendoza@preciazo.pe", "Preciazo2026!");
        mvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("carlos.mendoza@preciazo.pe"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_BUYER"));
    }

    @Test
    void shoppingListItemsAndBudgetWorkWithJwt() throws Exception {
        String token = signIn("carlos.mendoza@preciazo.pe", "Preciazo2026!");

        MvcResult createdList = mvc.perform(post("/api/v1/shopping-lists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Canasta Quincenal\",\"currency\":\"PEN\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long listId = ((Number) JsonPath.read(createdList.getResponse().getContentAsString(), "$.id")).longValue();

        MvcResult createdItem = mvc.perform(post("/api/v1/shopping-lists/" + listId + "/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productName\":\"Arroz Extra\",\"quantity\":2,\"unitLabel\":\"kg\",\"unitPrice\":3.90}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(7.80))
                .andReturn();
        long itemId = ((Number) JsonPath.read(createdItem.getResponse().getContentAsString(), "$.id")).longValue();

        mvc.perform(patch("/api/v1/shopping-lists/" + listId + "/items/" + itemId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"checked\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checked").value(true));

        mvc.perform(patch("/api/v1/shopping-lists/" + listId + "/budget")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"budgetLimit\":200.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budgetLimit").value(200.00));

        mvc.perform(delete("/api/v1/shopping-lists/" + listId + "/items/" + itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void catalogMerchantVerificationRoutingAndAnalyticsWorkEndToEnd() throws Exception {
        String buyerToken = signIn("carlos.mendoza@preciazo.pe", "Preciazo2026!");
        String merchantToken = signIn("alberto.ramos@preciazo.pe", "Preciazo2026!");

        // Catalog public endpoints
        mvc.perform(get("/api/v1/catalog/stores")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));
        mvc.perform(get("/api/v1/catalog/deals")).andExpect(status().isOk()).andExpect(jsonPath("$[0].discountBadge").isNotEmpty());
        mvc.perform(get("/api/v1/catalog/compare")).andExpect(status().isOk())
                .andExpect(jsonPath("$.storeRankings[0].bestOption").value(true))
                .andExpect(jsonPath("$.items.length()").value(7));

        // Merchant management
        mvc.perform(get("/api/v1/merchant/store").header("Authorization", "Bearer " + merchantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruc").value("20601234567"));
        mvc.perform(get("/api/v1/merchant/inventory").header("Authorization", "Bearer " + merchantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
        MvcResult promoRes = mvc.perform(post("/api/v1/merchant/promotions")
                        .header("Authorization", "Bearer " + merchantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productName\":\"Pan de Molde\",\"promoPrice\":4.20,\"originalPrice\":6.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.discountPercent").value(30))
                .andReturn();
        long promoId = ((Number) JsonPath.read(promoRes.getResponse().getContentAsString(), "$.id")).longValue();
        mvc.perform(delete("/api/v1/merchant/promotions/" + promoId).header("Authorization", "Bearer " + merchantToken))
                .andExpect(status().isNoContent());

        // Community price verification
        MvcResult verifRes = mvc.perform(post("/api/v1/verifications")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storeName\":\"Metro Miraflores\",\"productName\":\"Pan de Molde\",\"publishedPrice\":4.50,\"reportedPrice\":4.10}"))
                .andExpect(status().isCreated())
                .andReturn();
        long reportId = ((Number) JsonPath.read(verifRes.getResponse().getContentAsString(), "$.id")).longValue();
        mvc.perform(post("/api/v1/verifications/" + reportId + "/confirm").header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Route optimization
        mvc.perform(post("/api/v1/routes/optimize")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"transportMode\":\"car\",\"excludedStores\":[\"Wong Surco\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stops.length()").value(2))
                .andExpect(jsonPath("$.transportMode").value("car"));

        // Analytics summary
        mvc.perform(get("/api/v1/analytics/merchant-summary").header("Authorization", "Bearer " + merchantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInventoryItems").value(5))
                .andExpect(jsonPath("$.availableItems").value(4));

        // OpenAPI docs include bearerAuth and all contexts
        String apiDocs = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(apiDocs).contains("bearerAuth", "/api/v1/authentication/sign-in", "/api/v1/shopping-lists",
                "/api/v1/catalog/compare", "/api/v1/merchant/inventory", "/api/v1/verifications",
                "/api/v1/routes/optimize", "/api/v1/analytics/merchant-summary");
    }
}
