package org.example.bootReactiveSecurity.auth.service;

import org.example.bootReactiveSecurity.admin.dto.UserRequest;
import org.example.bootReactiveSecurity.auth.entity.User;
import org.example.bootReactiveSecurity.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User user;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("1")
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();

        userRequest = new UserRequest("1", "testuser", "test@example.com", "password", true, List.of("ROLE_USER"));
    }

    @Test
    void register_ShouldReturnCreatedUser() {
        when(userRepository.findById("1")).thenReturn(Mono.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));

        Mono<ResponseEntity<User>> result = authService.register(userRequest);

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals("testuser", responseEntity.getBody().getUsername());
                })
                .verifyComplete();

        verify(userRepository, times(1)).findById("1");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenUserExists() {
        when(userRepository.findById("1")).thenReturn(Mono.just(user));

        Mono<ResponseEntity<User>> result = authService.register(userRequest);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("User already exists"))
                .verify();

        verify(userRepository, times(1)).findById("1");
        verify(userRepository, never()).save(any(User.class));
    }
}
