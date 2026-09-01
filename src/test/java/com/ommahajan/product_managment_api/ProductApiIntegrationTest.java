package com.ommahajan.product_managment_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ommahajan.product_managment_api.dto.request.LoginRequest;
import com.ommahajan.product_managment_api.dto.request.ProductRequest;
import com.ommahajan.product_managment_api.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String registerAndLogin(String username) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(username, "password123"))))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, "password123"))))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("accessToken").asText();
    }

    @Test
    void fullProductLifecycle_createReadUpdateDelete() throws Exception {
        String accessToken = registerAndLogin("integrationuser1");

        // CREATE
        MvcResult createResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductRequest("Integration Test Product"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Integration Test Product"))
                .andReturn();

        String createdBody = createResult.getResponse().getContentAsString();
        int productId = objectMapper.readTree(createdBody).get("id").asInt();

        // READ (single)
        mockMvc.perform(get("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Integration Test Product"));

        // READ (list, paginated)
        mockMvc.perform(get("/api/v1/products?page=0&size=10")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // UPDATE
        mockMvc.perform(put("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductRequest("Updated Product Name"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Updated Product Name"))
                .andExpect(jsonPath("$.modifiedOn").exists());

        // GET ITEMS (empty, no items created)
        mockMvc.perform(get("/api/v1/products/" + productId + "/items")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());

        // DELETE
        mockMvc.perform(delete("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        // CONFIRM DELETED
        mockMvc.perform(get("/api/v1/products/" + productId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductRequest("Should Fail"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenRotation_oldTokenBecomesInvalidAfterUse() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("integrationuser2", "password123"))))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("integrationuser2", "password123"))))
                .andExpect(status().isOk())
                .andReturn();

        String loginBody = loginResult.getResponse().getContentAsString();
        String originalRefreshToken = objectMapper.readTree(loginBody).get("refreshToken").asText();

        // First refresh should succeed
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + originalRefreshToken + "\"}"))
                .andExpect(status().isOk());

        // Reusing the same (now-rotated) refresh token should fail
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + originalRefreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}