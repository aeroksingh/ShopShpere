package com.shopsphere.shopsphere.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.shopsphere.dto.auth.AuthResponse;
import com.shopsphere.shopsphere.dto.auth.LoginRequest;
import com.shopsphere.shopsphere.dto.auth.RegisterRequest;
import com.shopsphere.shopsphere.exception.DuplicateResourceException;
import com.shopsphere.shopsphere.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @WebMvcTest loads ONLY the web layer (this controller + Jackson + validation +
 * our @RestControllerAdvice) -- no database, no full Spring context, no real
 * AuthService. addFilters = false skips the Spring Security filter chain so we
 * can test controller behavior and validation in isolation; security itself
 * is exercised separately (see the note at the end of this file).
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/register with valid data returns 201 and the auth response")
    void register_validRequest_returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Jane Doe")
                .email("jane@example.com")
                .password("password123")
                .build();

        AuthResponse mockResponse = AuthResponse.builder()
                .accessToken("fake-jwt-token")
                .tokenType("Bearer")
                .userId(1L)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .role("ROLE_CUSTOMER")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("fake-jwt-token"))
                .andExpect(jsonPath("$.data.role").value("ROLE_CUSTOMER"));
    }

    @Test
    @DisplayName("POST /api/auth/register with a blank email returns 400 with a field-level error")
    void register_blankEmail_returns400WithFieldError() throws Exception {
        // Missing email entirely -- @NotBlank + @Email should both be triggered by MethodArgumentNotValidException
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .fullName("Jane Doe")
                .email("")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register with a weak password (no digit) returns 400")
    void register_weakPassword_returns400() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .fullName("Jane Doe")
                .email("jane@example.com")
                .password("onlyletters") // fails the @Pattern requiring a digit
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register maps DuplicateResourceException to 409 via GlobalExceptionHandler")
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Jane Doe")
                .email("jane@example.com")
                .password("password123")
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("An account with this email already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    @DisplayName("POST /api/auth/login with valid credentials returns 200")
    void login_validCredentials_returns200() throws Exception {
        LoginRequest request = LoginRequest.builder().email("jane@example.com").password("password123").build();
        AuthResponse mockResponse = AuthResponse.builder()
                .accessToken("fake-jwt-token").tokenType("Bearer").userId(1L)
                .email("jane@example.com").role("ROLE_CUSTOMER").build();

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("fake-jwt-token"));
    }

    // NOTE: addFilters = false above means Spring Security's filter chain (including our
    // JwtAuthenticationFilter) does NOT run in this test class -- intentional, since we're
    // testing controller + validation behavior here, not authentication. To test the
    // security layer itself, write a separate @SpringBootTest (full context) hitting a
    // protected endpoint with and without a valid Authorization header.
}
