package com.github.suvratking.bootReactiveSecurity.auth.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import com.github.suvratking.bootReactiveSecurity.admin.dto.UserRequest;
import com.github.suvratking.bootReactiveSecurity.auth.entity.User;
import com.github.suvratking.bootReactiveSecurity.auth.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Service class for authentication-related operations.
 * Handles user registration logic.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user.
     * Checks if a user with the given ID already exists. If so, returns an error.
     * Otherwise, encodes the password and saves the new user.
     *
     * @param userRequest a {@link Mono} containing the {@link UserRequest} with the new user's details.
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the registered {@link User}.
     * @throws RuntimeException if the user with the specified ID already exists.
     */
    public Mono<ResponseEntity<User>> register(Mono<UserRequest> userRequest) {
        return userRequest
                .flatMap(req -> userRepository
                .findById(req.id())
                .flatMap(_ -> Mono.<User>error(new RuntimeException("User already exists")))
                .switchIfEmpty(Mono.defer(() ->
                    this
                        .getMaxId()
                        .flatMap(id -> {
                            User newUser = new User();
                            BeanUtils.copyProperties(req, newUser);
                            newUser.setPassword(passwordEncoder.encode(req.password()));
                            newUser.setId(id);
                            return userRepository.save(newUser);
                        })
                )))
                .map(ResponseEntity::ok);
    }

    @PostConstruct
    public void test() {
        userRepository
                .findTopByOrderByIdDesc()
                .subscribe(IO::println);
    }

    private Mono<Long> getMaxId() {
        return userRepository
                .findTopByOrderByIdDesc()
                .map(user -> user.getId() + 1L)
                .switchIfEmpty(Mono.just(0L));
    }

}
