package com.ommahajan.product_managment_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ommahajan.product_managment_api.dto.request.LoginRequest;
import com.ommahajan.product_managment_api.dto.request.RefreshRequest;
import com.ommahajan.product_managment_api.dto.request.RegisterRequest;
import com.ommahajan.product_managment_api.dto.response.AuthResponse;
import com.ommahajan.product_managment_api.exception.InvalidTokenException;
import com.ommahajan.product_managment_api.security.JwtAuthenticationFilter;
import com.ommahajan.product_managment_api.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void register_withValidRequest_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("newuser", "password123"))))
                .andExpect(status().isCreated());
    }

    @Test
    void register_withBlankUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("", "password123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withValidCredentials_returns200WithTokens() throws Exception {
        AuthResponse response = new AuthResponse("access-token", "refresh-token");
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("testuser", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidTokenException("Invalid username or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("testuser", "wrongpassword"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_withValidToken_returns200WithNewTokens() throws Exception {
        AuthResponse response = new AuthResponse("new-access-token", "new-refresh-token");
        when(authService.refresh("valid-refresh-token")).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest("valid-refresh-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    void refresh_withRevokedToken_returns401() throws Exception {
        when(authService.refresh("revoked-token"))
                .thenThrow(new InvalidTokenException("Refresh token has been revoked"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest("revoked-token"))))
                .andExpect(status().isUnauthorized());
    }
}