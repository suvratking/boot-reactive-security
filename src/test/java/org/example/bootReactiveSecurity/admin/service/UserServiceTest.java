package org.example.bootReactiveSecurity.admin.service;

import org.example.bootReactiveSecurity.admin.dto.UserRequest;
import org.example.bootReactiveSecurity.admin.dto.UserResponse;
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
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

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
    void findAll_ShouldReturnUserResponse() {
        when(userRepository.findAll()).thenReturn(Flux.just(user));

        Mono<ResponseEntity<UserResponse>> result = userService.findAll();

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals(1, responseEntity.getBody().users().size());
                    assertEquals("testuser", responseEntity.getBody().users().get(0).getUsername());
                })
                .verifyComplete();

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void createUser_ShouldReturnCreatedUser() {
        when(userRepository.findById("1")).thenReturn(Mono.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));

        Mono<ResponseEntity<User>> result = userService.createUser(Mono.just(userRequest));

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
    void createUser_ShouldThrowException_WhenUserExists() {
        when(userRepository.findById("1")).thenReturn(Mono.just(user));

        Mono<ResponseEntity<User>> result = userService.createUser(Mono.just(userRequest));

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.CONFLICT)
                .verify();

        verify(userRepository, times(1)).findById("1");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() {
        when(userRepository.findById("1")).thenReturn(Mono.just(user));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));

        Mono<ResponseEntity<User>> result = userService.updateUser(Mono.just(userRequest), "1");

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
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById("1")).thenReturn(Mono.empty());

        Mono<ResponseEntity<User>> result = userService.updateUser(Mono.just(userRequest), "1");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("User not found with id: 1"))
                .verify();

        verify(userRepository, times(1)).findById("1");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldReturnDeletedUser() {
        when(userRepository.findById("1")).thenReturn(Mono.just(user));
        when(userRepository.delete(any(User.class))).thenReturn(Mono.empty());

        Mono<ResponseEntity<User>> result = userService.deleteUser("1");

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals("1", responseEntity.getBody().getId());
                })
                .verifyComplete();

        verify(userRepository, times(1)).findById("1");
        verify(userRepository, times(1)).delete(any(User.class));
    }

    @Test
    void findUserById_ShouldReturnUser() {
        when(userRepository.findById("1")).thenReturn(Mono.just(user));

        Mono<ResponseEntity<User>> result = userService.findUserById("1");

        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
                    assertNotNull(responseEntity.getBody());
                    assertEquals("1", responseEntity.getBody().getId());
                })
                .verifyComplete();

        verify(userRepository, times(1)).findById("1");
    }
}
