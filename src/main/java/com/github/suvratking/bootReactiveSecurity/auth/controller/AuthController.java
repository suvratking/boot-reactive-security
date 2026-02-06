package com.github.suvratking.bootReactiveSecurity.auth.controller;

import jakarta.validation.Validation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import com.github.suvratking.bootReactiveSecurity.admin.dto.UserRequest;
import com.github.suvratking.bootReactiveSecurity.auth.config.JwtTokenProvider;
import com.github.suvratking.bootReactiveSecurity.auth.dto.AuthenticationRequest;
import com.github.suvratking.bootReactiveSecurity.auth.entity.User;
import com.github.suvratking.bootReactiveSecurity.auth.service.AuthService;
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

import jakarta.validation.Valid;

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
    public Mono<ResponseEntity<User>> register(@Validated @RequestBody Mono<UserRequest> userRequest) {
        /*var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var violations = validator.validate(userRequest);
        if (!violations.isEmpty()) {
            var message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(joining(", "));
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, message));
        }*/
        return authService.register(userRequest);
    }

    /**
     * Validates a registration request (version 1).
     *
     * @param userRequest a {@link Mono} containing the {@link Request} to be validated.
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the validated {@link Request}.
     */
    @PostMapping("/v1/register")
    public Mono<ResponseEntity<Request>> validate(@Valid @RequestBody Mono<Request> userRequest) {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var violations = validator.validate(userRequest);
        if (!violations.isEmpty()) {
            var message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(joining(", "));
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, message));
        }
        return userRequest.map(ResponseEntity::ok);
    }

}

@Getter
@Setter
class Request {
    @NotEmpty
    String id;
    @NotEmpty String username;
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
            flags = Pattern.Flag.CASE_INSENSITIVE) String email;
    @NotEmpty String password;
    boolean active;
    @NotEmpty List<String> roles;
}