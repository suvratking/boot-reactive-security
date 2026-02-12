package com.github.suvratking.bootReactiveSecurity.admin.controller;

import com.github.suvratking.bootReactiveSecurity.admin.dto.UserRequest;
import com.github.suvratking.bootReactiveSecurity.admin.dto.UserResponse;
import com.github.suvratking.bootReactiveSecurity.admin.service.UserService;
import com.github.suvratking.bootReactiveSecurity.auth.entity.User;
import com.github.suvratking.bootReactiveSecurity.auth.repository.UserRepository;
import com.github.suvratking.bootReactiveSecurity.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class AdminControllerTest {

    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .configureClient()
                .build();
    }

    @Test
    @WithMockUser
    void getUser_ShouldReturnUserResponse() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();
        UserResponse response = new UserResponse(List.of(user));

        when(userService.findAll()).thenReturn(Mono.just(ResponseEntity.ok(response)));

        webTestClient.get()
                .uri("/admin/user")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.users[0].id").isEqualTo("1")
                .jsonPath("$.users[0].username").isEqualTo("testuser");
    }

    @Test
    @WithMockUser
    void createUser_ShouldReturnCreatedUser() {
        UserRequest request = new UserRequest(1L, "testuser", "test@example.com", "password", true, List.of("ROLE_USER"));
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();

        when(userService.createUser(any())).thenReturn(Mono.just(ResponseEntity.ok(user)));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/admin/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.username").isEqualTo("testuser");
    }

    @Test
    @WithMockUser
    void updateUser_ShouldReturnUpdatedUser() {
        UserRequest request = new UserRequest(1L, "updateduser", "updated@example.com", "password", true, List.of("ROLE_USER"));
        User user = User.builder()
                .id(1L)
                .username("updateduser")
                .email("updated@example.com")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();

        when(userService.updateUser(any(), eq(1L))).thenReturn(Mono.just(ResponseEntity.ok(user)));

        webTestClient.mutateWith(csrf())
                .put()
                .uri("/admin/user/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("updateduser");
    }

    @Test
    @WithMockUser
    void deleteUser_ShouldReturnDeletedUser() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        when(userService.deleteUser(1L)).thenReturn(Mono.just(ResponseEntity.ok(user)));

        webTestClient.mutateWith(csrf())
                .delete()
                .uri("/admin/user/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1");
    }

    @Test
    @WithMockUser
    void getUserById_ShouldReturnUser() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        when(userService.findUserById(1L)).thenReturn(Mono.just(ResponseEntity.ok(user)));

        webTestClient.get()
                .uri("/admin/user/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.username").isEqualTo("testuser");
    }

    @Test
    void getUser_WithoutUser_ShouldReturnUnauthorized() {
        webTestClient.get()
                .uri("/admin/user")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
