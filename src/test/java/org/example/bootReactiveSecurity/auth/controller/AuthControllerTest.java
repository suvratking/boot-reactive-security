package org.example.bootReactiveSecurity.auth.controller;

import org.example.bootReactiveSecurity.admin.dto.UserRequest;
import org.example.bootReactiveSecurity.auth.config.JwtTokenProvider;
import org.example.bootReactiveSecurity.auth.dto.AuthenticationRequest;
import org.example.bootReactiveSecurity.auth.entity.User;
import org.example.bootReactiveSecurity.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class AuthControllerTest {

    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ReactiveAuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    @Test
    void login_ShouldReturnTokenAndAuthorizationHeader() {
        AuthenticationRequest authRequest = new AuthenticationRequest("testuser", "password");
        
        // Mock the authentication manager to return an authentication token with authorities
        var authentication = new UsernamePasswordAuthenticationToken(
                "testuser", 
                "password",
                List.of(() -> "ROLE_USER")
        );
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(Mono.just(authentication));

        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.access_token").exists()
                .jsonPath("$.access_token").isNotEmpty();
    }

    @Test
    void login_ShouldReturnAuthorizationHeaderWithBearer() {
        AuthenticationRequest authRequest = new AuthenticationRequest("testuser", "password");
        
        var authentication = new UsernamePasswordAuthenticationToken(
                "testuser", 
                "password",
                List.of(() -> "ROLE_USER")
        );
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(Mono.just(authentication));

        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Authorization")
                .expectHeader().value("Authorization", header -> 
                    org.junit.jupiter.api.Assertions.assertTrue(header.startsWith("Bearer "))
                );
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() {
        AuthenticationRequest authRequest = new AuthenticationRequest("testuser", "wrongpassword");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(Mono.error(new RuntimeException("Invalid credentials")));

        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void login_WithMissingUsername_ShouldReturnBadRequest() {
        String jsonBody = "{\"password\": \"password\"}";
        
        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void login_WithMissingPassword_ShouldReturnBadRequest() {
        String jsonBody = "{\"username\": \"testuser\"}";
        
        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void register_ShouldReturnCreatedUser() {
        UserRequest userRequest = new UserRequest(
                "newuser", 
                "newuser", 
                "newuser@example.com", 
                "password", 
                true, 
                List.of("ROLE_USER")
        );
        
        User registeredUser = User.builder()
                .id("newuser")
                .username("newuser")
                .email("newuser@example.com")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();
        
        when(authService.register(any(UserRequest.class)))
                .thenReturn(Mono.just(ResponseEntity.ok(registeredUser)));

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("newuser")
                .jsonPath("$.username").isEqualTo("newuser")
                .jsonPath("$.email").isEqualTo("newuser@example.com");
    }

    @Test
    void register_WithInvalidEmail_ShouldReturnBadRequest() {
        UserRequest userRequest = new UserRequest(
                "newuser", 
                "newuser", 
                "invalid-email", 
                "password", 
                true, 
                List.of("ROLE_USER")
        );

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void register_WithMissingRequiredField_ShouldReturnBadRequest() {
        String jsonBody = "{\"username\": \"newuser\", \"email\": \"newuser@example.com\"}";

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void register_WithDuplicateUser_ShouldReturnConflict() {
        UserRequest userRequest = new UserRequest(
                "existinguser", 
                "existinguser", 
                "existing@example.com", 
                "password", 
                true, 
                List.of("ROLE_USER")
        );
        
        when(authService.register(any(UserRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("User already exists")));

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest)
                .exchange()
                .expectStatus().is5xxServerError();
    }

}
