package com.github.suvratking.bootReactiveSecurity.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void authenticationRequest_WithValidData_ShouldBeValid() {
        AuthenticationRequest request = new AuthenticationRequest("testuser", "password123");

        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void authenticationRequest_WithNullUsername_ShouldBeInvalid() {
        AuthenticationRequest request = new AuthenticationRequest(null, "password123");

        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void authenticationRequest_WithBlankUsername_ShouldBeInvalid() {
        AuthenticationRequest request = new AuthenticationRequest("", "password123");

        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void authenticationRequest_WithWhitespaceUsername_ShouldBeInvalid() {
        AuthenticationRequest request = new AuthenticationRequest("   ", "password123");

        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void authenticationRequest_WithNullPassword_ShouldBeInvalid() {
        AuthenticationRequest request = new AuthenticationRequest("testuser", null);

        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void authenticationRequest_WithBlankPassword_ShouldBeInvalid() {
        AuthenticationRequest request = new AuthenticationRequest("testuser", "");

        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void authenticationRequest_WithWhitespacePassword_ShouldBeInvalid() {
        AuthenticationRequest request = new AuthenticationRequest("testuser", "   ");

        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void authenticationRequest_WithBothFieldsInvalid_ShouldHaveTwoViolations() {
        AuthenticationRequest request = new AuthenticationRequest("", "");

        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        assertEquals(2, violations.size());
    }

    @Test
    void authenticationRequest_Equality_ShouldBeEqual() {
        AuthenticationRequest request1 = new AuthenticationRequest("testuser", "password123");
        AuthenticationRequest request2 = new AuthenticationRequest("testuser", "password123");

        assertEquals(request1, request2);
    }

    @Test
    void authenticationRequest_Equality_ShouldNotBeEqual() {
        AuthenticationRequest request1 = new AuthenticationRequest("testuser", "password123");
        AuthenticationRequest request2 = new AuthenticationRequest("otheruser", "password123");

        assertNotEquals(request1, request2);
    }

    @Test
    void authenticationRequest_ToString_ShouldContainFields() {
        AuthenticationRequest request = new AuthenticationRequest("testuser", "password123");

        String toString = request.toString();

        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("password123"));
    }

}
