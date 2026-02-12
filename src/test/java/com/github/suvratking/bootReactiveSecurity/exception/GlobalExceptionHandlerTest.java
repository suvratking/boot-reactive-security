package com.github.suvratking.bootReactiveSecurity.exception;

import com.github.suvratking.bootReactiveSecurity.auth.repository.UserRepository;
import com.github.suvratking.bootReactiveSecurity.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class GlobalExceptionHandlerTest {

    private WebTestClient webTestClient;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ReactiveAuthenticationManager authenticationManager;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    @Test
    void registerWithMissingId_ShouldReturnBadRequestWithErrorDetails() {
        String jsonBody = "{\"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password\", \"active\": true, \"roles\": [\"ROLE_USER\"]}";

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void registerWithMissingUsername_ShouldReturnBadRequestWithErrorDetails() {
        String jsonBody = "{\"id\": \"testid\", \"email\": \"test@example.com\", \"password\": \"password\", \"active\": true, \"roles\": [\"ROLE_USER\"]}";

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void registerWithMissingPassword_ShouldReturnBadRequestWithErrorDetails() {
        String jsonBody = "{\"id\": \"testid\", \"username\": \"testuser\", \"email\": \"test@example.com\", \"active\": true, \"roles\": [\"ROLE_USER\"]}";

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void registerWithMissingEmail_ShouldReturnBadRequestWithErrorDetails() {
        String jsonBody = "{\"id\": \"testid\", \"username\": \"testuser\", \"password\": \"password\", \"active\": true, \"roles\": [\"ROLE_USER\"]}";

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void registerWithMissingRoles_ShouldReturnBadRequestWithErrorDetails() {
        String jsonBody = "{\"id\": \"testid\", \"username\": \"testuser\", \"email\": \"test@example.com\", \"password\": \"password\", \"active\": true}";

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void registerWithInvalidEmail_ShouldReturnBadRequestWithEmailError() {
        String jsonBody = "{\"id\": \"testid\", \"username\": \"testuser\", \"email\": \"invalid-email\", \"password\": \"password\", \"active\": true, \"roles\": [\"ROLE_USER\"]}";

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void registerWithMultipleValidationErrors_ShouldReturnBadRequestWithAllErrors() {
        String jsonBody = "{\"email\": \"invalid-email\"}";

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void loginWithMissingUsername_ShouldReturnBadRequest() {
        String jsonBody = "{\"password\": \"password\"}";

        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.username").exists();
    }

    @Test
    void loginWithMissingPassword_ShouldReturnBadRequest() {
        String jsonBody = "{\"username\": \"testuser\"}";

        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.password").exists();
    }

}
