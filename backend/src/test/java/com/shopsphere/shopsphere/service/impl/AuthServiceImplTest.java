package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.auth.AuthResponse;
import com.shopsphere.shopsphere.dto.auth.LoginRequest;
import com.shopsphere.shopsphere.dto.auth.RegisterRequest;
import com.shopsphere.shopsphere.entity.Role;
import com.shopsphere.shopsphere.entity.User;
import com.shopsphere.shopsphere.exception.DuplicateResourceException;
import com.shopsphere.shopsphere.repository.UserRepository;
import com.shopsphere.shopsphere.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = User.builder()
                .id(1L)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .password("hashed-password")
                .role(Role.ROLE_CUSTOMER)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("register() hashes the password and never stores it in plain text")
    void register_hashesPasswordBeforeSaving() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Jane Doe")
                .email("jane@example.com")
                .password("plainPassword123")
                .build();

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateAccessToken(savedUser)).thenReturn("fake-jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getRole()).isEqualTo("ROLE_CUSTOMER");

        // The key assertion: whatever gets persisted must carry the HASHED password, never the raw one
        verify(userRepository).save(argThat(user -> user.getPassword().equals("hashed-password")));
        verify(passwordEncoder).encode("plainPassword123");
    }

    @Test
    @DisplayName("register() rejects an email that's already taken, before ever touching the encoder")
    void register_rejectsDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Jane Doe")
                .email("jane@example.com")
                .password("plainPassword123")
                .build();

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login() delegates credential checking to AuthenticationManager and returns a token")
    void login_authenticatesAndReturnsToken() {
        LoginRequest request = LoginRequest.builder().email("jane@example.com").password("plainPassword123").build();

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(savedUser));
        when(jwtService.generateAccessToken(savedUser)).thenReturn("fake-jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("fake-jwt-token");
        assertThat(response.getEmail()).isEqualTo("jane@example.com");

        // Verifies AuthenticationManager was actually asked to authenticate --
        // this is what triggers the BCrypt password comparison under the hood
        verify(authenticationManager).authenticate(any());
    }
}
