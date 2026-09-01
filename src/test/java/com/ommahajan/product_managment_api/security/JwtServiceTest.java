package com.ommahajan.product_managment_api.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        String testSecret = Base64.getEncoder().encodeToString("test-secret-key-must-be-32-bytes!".getBytes());
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecret);
        ReflectionTestUtils.setField(jwtService, "expiration", 900000L); // 15 min
    }

    @Test
    void generateToken_thenExtractUsername_returnsOriginalUsername() {
        String token = jwtService.generateToken("testuser", "USER");

        assertThat(jwtService.extractUsername(token)).isEqualTo("testuser");
    }

    @Test
    void generateToken_thenExtractRole_returnsOriginalRole() {
        String token = jwtService.generateToken("testuser", "ADMIN");

        assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void isTokenValid_forFreshToken_returnsTrue() {
        String token = jwtService.generateToken("testuser", "USER");

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_forExpiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L); // already expired
        String token = jwtService.generateToken("testuser", "USER");

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_forMalformedToken_returnsFalse() {
        assertThat(jwtService.isTokenValid("not.a.valid.jwt")).isFalse();
    }

    @Test
    void isTokenExpired_forExpiredToken_throwsExpiredJwtException() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
        String token = jwtService.generateToken("testuser", "USER");

        // isTokenExpired itself calls extractAllClaims, which throws before
        // the expiry check runs — isTokenValid is what safely catches this.
        assertThatThrownBy(() -> jwtService.isTokenExpired(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}