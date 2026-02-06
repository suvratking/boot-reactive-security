package com.github.suvratking.bootReactiveSecurity.auth.controller;

import com.github.suvratking.bootReactiveSecurity.admin.dto.UserRequest;
import com.github.suvratking.bootReactiveSecurity.auth.config.JwtTokenProvider;
import com.github.suvratking.bootReactiveSecurity.auth.dto.AuthenticationRequest;
import com.github.suvratking.bootReactiveSecurity.auth.entity.User;
import com.github.suvratking.bootReactiveSecurity.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * Controller for authentication-related operations.
 * Handles user login and registration.
 *
 * @author hantsy
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication API", description = "The Authentication API for user login and registration")
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final ReactiveAuthenticationManager authenticationManager;
    private final AuthService authService;

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param authRequest a {@link Mono} containing the {@link AuthenticationRequest} with username and password.
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the access token and authorization header.
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token.")
    public Mono<ResponseEntity<?>> login(
            @Valid @RequestBody Mono<AuthenticationRequest> authRequest) {

        return authRequest
                .flatMap(login -> this.authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(
                                login.username(), login.password()))
                        .map(this.tokenProvider::createToken))
                .map(jwt -> {
                    var httpHeaders = new HttpHeaders();
                    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
                    var tokenBody = Map.of("access_token", jwt);
                    return new ResponseEntity<>(tokenBody, httpHeaders, HttpStatus.OK);
                });

    }

    /**
     * Registers a new user.
     *
     * @param userRequest a {@link Mono} containing the {@link UserRequest} with the new user's details.
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the registered {@link User}.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user with the given details.")
    public Mono<ResponseEntity<User>> register(@Valid @RequestBody Mono<UserRequest> userRequest) {
        return userRequest.flatMap(request -> {
            var validator = Validation.buildDefaultValidatorFactory().getValidator();
            var violations = validator.validate(request);
            if (!violations.isEmpty()) {
                var message = violations.stream()
                        .map(v -> v.getPropertyPath() + " " + v.getMessage())
                        .collect(joining(", "));
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, message));
            }
            return authService.register(Mono.just(request));
        });
    }

}