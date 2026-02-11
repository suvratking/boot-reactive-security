package org.example.bootReactiveSecurity.admin.dto;

import com.github.suvratking.bootReactiveSecurity.admin.dto.UserRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void userRequest_WithValidData_ShouldBeValid() {
        UserRequest request = new UserRequest(
                1L,
                "testuser",
                "test@example.com",
                "password123",
                true,
                List.of("ROLE_USER")
        );

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void userRequest_WithNullId_ShouldBeInvalid() {
        UserRequest request = new UserRequest(
                null,
                "testuser",
                "test@example.com",
                "password123",
                true,
                List.of("ROLE_USER")
        );

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("id")));
    }

    @Test
    void userRequest_WithEmptyId_ShouldBeInvalid() {
        UserRequest request = new UserRequest(
                0L,
                "testuser",
                "test@example.com",
                "password123",
                true,
                List.of("ROLE_USER")
        );

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("id")));
    }

    @Test
    void userRequest_WithNullUsername_ShouldBeInvalid() {
        UserRequest request = new UserRequest(
                1L,
                null,
                "test@example.com",
                "password123",
                true,
                List.of("ROLE_USER")
        );

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void userRequest_WithInvalidEmail_ShouldBeInvalid() {
        UserRequest request = new UserRequest(
                1L,
                "testuser",
                "invalid-email",
                "password123",
                true,
                List.of("ROLE_USER")
        );

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void userRequest_WithNullEmail_ShouldBeInvalid() {
        UserRequest request = new UserRequest(
                1L,
                "testuser",
                null,
                "password123",
                true,
                List.of("ROLE_USER")
        );

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void userRequest_WithNullPassword_ShouldBeInvalid() {
        UserRequest request = new UserRequest(
                1L,
                "testuser",
                "test@example.com",
                null,
                true,
                List.of("ROLE_USER")
        );

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void userRequest_WithNullRoles_ShouldBeInvalid() {
        UserRequest request = new UserRequest(
                1L,
                "testuser",
                "test@example.com",
                "password123",
                true,
                null
        );

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("roles")));
    }

    @Test
    void userRequest_WithEmptyRoles_ShouldBeInvalid() {
        UserRequest request = new UserRequest(
                1L,
                "testuser",
                "test@example.com",
                "password123",
                true,
                List.of()
        );

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("roles")));
    }

    @Test
    void userRequest_WithMultipleRoles_ShouldBeValid() {
        UserRequest request = new UserRequest(
                1L,
                "testuser",
                "test@example.com",
                "password123",
                true,
                List.of("ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER")
        );

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void userRequest_WithActiveFalse_ShouldBeValid() {
        UserRequest request = new UserRequest(
                1L,
                "testuser",
                "test@example.com",
                "password123",
                false,
                List.of("ROLE_USER")
        );

        Set<ConstraintViolation<UserRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void userRequest_Equality_ShouldBeEqual() {
        UserRequest request1 = new UserRequest(
                1L,
                "testuser",
                "test@example.com",
                "password123",
                true,
                List.of("ROLE_USER")
        );

        UserRequest request2 = new UserRequest(
                1L,
                "testuser",
                "test@example.com",
                "password123",
                true,
                List.of("ROLE_USER")
        );

        assertEquals(request1, request2);
    }

    @Test
    void userRequest_ToString_ShouldContainFields() {
        UserRequest request = new UserRequest(
                1L,
                "testuser",
                "test@example.com",
                "password123",
                true,
                List.of("ROLE_USER")
        );

        String toString = request.toString();

        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test@example.com"));
    }

}
