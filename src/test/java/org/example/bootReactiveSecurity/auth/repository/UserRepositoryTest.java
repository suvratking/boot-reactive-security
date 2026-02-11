package org.example.bootReactiveSecurity.auth.repository;

import com.github.suvratking.bootReactiveSecurity.auth.entity.User;
import com.github.suvratking.bootReactiveSecurity.auth.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;

import org.springframework.test.annotation.DirtiesContext;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@DirtiesContext
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll().block();
        
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();
    }

    @Test
    void save_ShouldPersistUser() {
        userRepository.save(testUser)
                .as(StepVerifier::create)
                .assertNext(savedUser -> {
                    assertNotNull(savedUser.getId());
                    assertEquals("testuser", savedUser.getUsername());
                    assertEquals("test@example.com", savedUser.getEmail());
                })
                .verifyComplete();
    }

    @Test
    void findById_ShouldReturnUser() {
        userRepository.save(testUser).block();

        userRepository.findById(1L)
                .as(StepVerifier::create)
                .assertNext(foundUser -> {
                    assertEquals(1L, foundUser.getId());
                    assertEquals("testuser", foundUser.getUsername());
                })
                .verifyComplete();
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        userRepository.findById(1L)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void findByUsername_ShouldReturnUser() {
        userRepository.save(testUser).block();

        userRepository.findByUsername("testuser")
                .as(StepVerifier::create)
                .assertNext(foundUser -> {
                    assertEquals("testuser", foundUser.getUsername());
                    assertEquals("test@example.com", foundUser.getEmail());
                })
                .verifyComplete();
    }

    @Test
    void findByUsername_WithNonExistentUsername_ShouldReturnEmpty() {
        userRepository.findByUsername("non-existent-username")
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        User user2 = User.builder()
                .id(2L)
                .username("testuser2")
                .email("test2@example.com")
                .password("encodedPassword")
                .active(true)
                .roles(List.of("ROLE_ADMIN"))
                .build();

        userRepository.save(testUser).block();
        userRepository.save(user2).block();

        userRepository.findAll()
                .collectList()
                .as(StepVerifier::create)
                .assertNext(users -> {
                    assertEquals(2, users.size());
                    assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("testuser")));
                    assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("testuser2")));
                })
                .verifyComplete();
    }

    @Test
    void findAll_WhenEmpty_ShouldReturnEmptyList() {
        userRepository.findAll()
                .collectList()
                .as(StepVerifier::create)
                .assertNext(users -> {
                    assertEquals(0, users.size());
                })
                .verifyComplete();
    }

    @Test
    void delete_ShouldRemoveUser() {
        User savedUser = userRepository.save(testUser).block();
        assertNotNull(savedUser);

        userRepository.delete(savedUser)
                .as(StepVerifier::create)
                .verifyComplete();

        userRepository.findById(1L)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void deleteById_ShouldRemoveUserById() {
        userRepository.save(testUser).block();

        userRepository.deleteById(1L)
                .as(StepVerifier::create)
                .verifyComplete();

        userRepository.findById(1L)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void existsById_WhenUserExists_ShouldReturnTrue() {
        userRepository.save(testUser).block();

        userRepository.existsById(1L)
                .as(StepVerifier::create)
                .assertNext(Assertions::assertTrue)
                .verifyComplete();
    }

    @Test
    void existsById_WhenUserNotExists_ShouldReturnFalse() {
        userRepository.existsById(1L)
                .as(StepVerifier::create)
                .assertNext(Assertions::assertFalse)
                .verifyComplete();
    }

    @Test
    void count_ShouldReturnNumberOfUsers() {
        User user2 = User.builder()
                .id(2L)
                .username("testuser2")
                .email("test2@example.com")
                .password("encodedPassword")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();

        userRepository.save(testUser).block();
        userRepository.save(user2).block();

        userRepository.count()
                .as(StepVerifier::create)
                .assertNext(count -> assertEquals(2, count))
                .verifyComplete();
    }

    @Test
    void count_WhenEmpty_ShouldReturnZero() {
        userRepository.count()
                .as(StepVerifier::create)
                .assertNext(count -> assertEquals(0, count))
                .verifyComplete();
    }

}
