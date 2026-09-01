package com.ommahajan.product_managment_api.service;

import com.ommahajan.product_managment_api.dto.request.LoginRequest;
import com.ommahajan.product_managment_api.dto.request.RegisterRequest;
import com.ommahajan.product_managment_api.dto.response.AuthResponse;
import com.ommahajan.product_managment_api.entity.AppUser;
import com.ommahajan.product_managment_api.entity.RefreshToken;
import com.ommahajan.product_managment_api.entity.Role;
import com.ommahajan.product_managment_api.exception.InvalidTokenException;
import com.ommahajan.product_managment_api.repository.AppUserRepository;
import com.ommahajan.product_managment_api.security.JwtService;
import com.ommahajan.product_managment_api.security.RefreshTokenService;
import com.ommahajan.product_managment_api.service.implementation.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private AppUser existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new AppUser();
        existingUser.setId(1);
        existingUser.setUsername("testuser");
        existingUser.setPassword("hashedPassword");
        existingUser.setRole(Role.USER);
    }

    @Test
    void register_whenUsernameAvailable_savesUser() {
        RegisterRequest request = new RegisterRequest("newuser", "password123");
        when(appUserRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        authService.register(request);

        verify(appUserRepository, times(1)).save(any(AppUser.class));
    }

    @Test
    void register_whenUsernameTaken_throwsException() {
        RegisterRequest request = new RegisterRequest("testuser", "password123");
        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");

        verify(appUserRepository, never()).save(any(AppUser.class));
    }

    @Test
    void login_withValidCredentials_returnsTokens() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token-value");

        when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken("testuser", "USER")).thenReturn("access-token-value");
        when(refreshTokenService.createFor(existingUser)).thenReturn(refreshToken);

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token-value");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-value");
    }

    @Test
    void login_withInvalidCredentials_throwsException() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refresh_withValidToken_rotatesAndReturnsNewTokens() {
        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("old-refresh-token");
        oldToken.setUser(existingUser);
        oldToken.setExpiryDate(Instant.now().plusSeconds(3600));

        RefreshToken newToken = new RefreshToken();
        newToken.setToken("new-refresh-token");

        when(refreshTokenService.verify("old-refresh-token")).thenReturn(oldToken);
        when(refreshTokenService.createFor(existingUser)).thenReturn(newToken);
        when(jwtService.generateToken("testuser", "USER")).thenReturn("new-access-token");

        AuthResponse response = authService.refresh("old-refresh-token");

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        verify(refreshTokenService, times(1)).revoke(oldToken);
    }
}