package com.github.suvratking.bootReactiveSecurity.auth.integration;

import com.github.suvratking.bootReactiveSecurity.admin.dto.UserRequest;
import com.github.suvratking.bootReactiveSecurity.auth.entity.User;
import com.github.suvratking.bootReactiveSecurity.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@Disabled("Requires a real MongoDB-backed repository; excluded from DB-free test runs.")
class AuthIntegrationTest {

    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .configureClient()
                .build();
        
        // Clear the repository before each test
        userRepository.deleteAll().block();
    }

    @Test
    void register_ShouldCreateNewUser_AndThenLoginShouldWork() {
        UserRequest registerRequest = new UserRequest(
                1L,
                "integrationuser",
                "integration@example.com",
                "password123",
                true,
                List.of("ROLE_USER")
        );

        // Register the user
        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("integrationuser");

        // Verify user is created in database
        User savedUser = userRepository.findByUsername("integrationuser").block();
        org.junit.jupiter.api.Assertions.assertNotNull(savedUser);
        org.junit.jupiter.api.Assertions.assertEquals("integrationuser", savedUser.getUsername());
        org.junit.jupiter.api.Assertions.assertEquals("integration@example.com", savedUser.getEmail());
    }

    @Test
    void register_WithDuplicateUsername_ShouldFail() {
        UserRequest firstRequest = new UserRequest(
                1L,
                "duplicateuser",
                "first@example.com",
                "password123",
                true,
                List.of("ROLE_USER")
        );

        UserRequest secondRequest = new UserRequest(
                1L,
                "duplicateuser",
                "second@example.com",
                "password123",
                true,
                List.of("ROLE_USER")
        );

        // Register the first user - should succeed
        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(firstRequest)
                .exchange()
                .expectStatus().isOk();

        // Register the second user with same ID - should fail
        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(secondRequest)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void authenticatedEndpoint_WithValidToken_ShouldBeAccessible() {
        // Create a user
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();
        userRepository.save(user).block();

        // Access admin endpoint with authenticated user
        webTestClient.get()
                .uri("/admin/user/testuser")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("testuser");
    }

    @Test
    void unauthenticatedEndpoint_WithoutToken_ShouldReturnUnauthorized() {
        webTestClient.get()
                .uri("/admin/user")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void authEndpoint_ShouldNotRequireAuthentication() {
        UserRequest registerRequest = new UserRequest(
                1L,
                "publicuser",
                "public@example.com",
                "password123",
                true,
                List.of("ROLE_USER")
        );

        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "adminuser", roles = "ADMIN")
    void createUser_ShouldSucceedWithValidData() {
        UserRequest createRequest = new UserRequest(
                1L,
                "newadminuser",
                "newadmin@example.com",
                "password123",
                true,
                List.of("ROLE_ADMIN")
        );

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/admin/user")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("newadminuser");
    }

    @Test
    @WithMockUser(username = "adminuser", roles = "ADMIN")
    void getAllUsers_ShouldReturnUserList() {
        // Create multiple users
        User user1 = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();

        User user2 = User.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();

        userRepository.save(user1).block();
        userRepository.save(user2).block();

        // Get all users
        webTestClient.get()
                .uri("/admin/user")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.users.length()").isEqualTo(2);
    }

    @Test
    @WithMockUser(username = "adminuser", roles = "ADMIN")
    void updateUser_ShouldModifyExistingUser() {
        User existingUser = User.builder()
                .id(1L)
                .username("updateuser")
                .email("update@example.com")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();
        userRepository.save(existingUser).block();

        UserRequest updateRequest = new UserRequest(
                1L,
                "updatedusername",
                "updated@example.com",
                "newpassword123",
                true,
                List.of("ROLE_ADMIN")
        );

        webTestClient.mutateWith(csrf())
                .put()
                .uri("/admin/user/updateuser")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo("updated@example.com");
    }

    @Test
    @WithMockUser(username = "adminuser", roles = "ADMIN")
    void deleteUser_ShouldRemoveUser() {
        User userToDelete = User.builder()
                .id(1L)
                .username("deleteuser")
                .email("delete@example.com")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();
        userRepository.save(userToDelete).block();

        webTestClient.mutateWith(csrf())
                .delete()
                .uri("/admin/user/deleteuser")
                .exchange()
                .expectStatus().isOk();

        // Verify user is deleted
        User deletedUser = userRepository.findById(1L).block();
        org.junit.jupiter.api.Assertions.assertNull(deletedUser);
    }

    @Test
    @WithMockUser(username = "adminuser", roles = "ADMIN")
    void getUserById_ShouldReturnSpecificUser() {
        User user = User.builder()
                .id(1L)
                .username("specificuser")
                .email("specific@example.com")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();
        userRepository.save(user).block();

        webTestClient.get()
                .uri("/admin/user/specificuser")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("specificuser")
                .jsonPath("$.username").isEqualTo("specificuser");
    }

    @Test
    @WithMockUser(username = "adminuser", roles = "ADMIN")
    void getUserById_WithNonExistentId_ShouldReturn500() {
        webTestClient.get()
                .uri("/admin/user/nonexistent")
                .exchange()
                .expectStatus().is5xxServerError();
    }

}
