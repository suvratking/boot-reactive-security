package com.github.suvratking.bootReactiveSecurity.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.github.suvratking.bootReactiveSecurity.admin.dto.UserRequest;
import com.github.suvratking.bootReactiveSecurity.admin.dto.UserResponse;
import com.github.suvratking.bootReactiveSecurity.admin.service.UserService;
import com.github.suvratking.bootReactiveSecurity.auth.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller for administrative operations related to users.
 * Provides endpoints for creating, retrieving, updating, and deleting users.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    /**
     * Retrieves all users.
     *
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the {@link UserResponse} with the list of users.
     */
    @GetMapping("/user")
    public Mono<ResponseEntity<UserResponse>> getUser() {
        return userService.findAll();
    }

    /**
     * Creates a new user.
     *
     * @param user a {@link Mono} containing the {@link UserRequest} with the details of the user to be created.
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the created {@link User}.
     */
    @PostMapping("/user")
    public Mono<ResponseEntity<User>> createUser(@Valid @RequestBody Mono<UserRequest> user) {
        return userService.createUser(user);
    }

    /**
     * Updates an existing user.
     *
     * @param user a {@link Mono} containing the {@link UserRequest} with the updated details of the user.
     * @param id   the unique identifier of the user to be updated.
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the updated {@link User}.
     */
    @PutMapping("/user/{id}")
    public Mono<ResponseEntity<User>> updateUser(@Valid @RequestBody Mono<UserRequest> user, @PathVariable String id) {
        return userService.updateUser(user, id);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id the unique identifier of the user to be deleted.
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the deleted {@link User}.
     */
    @DeleteMapping("/user/{id}")
    public Mono<ResponseEntity<User>> deleteUser(@PathVariable String id) {
        return userService.deleteUser(id);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id the unique identifier of the user to retrieve.
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the retrieved {@link User}.
     */
    @GetMapping("/user/{id}")
    public Mono<ResponseEntity<User>> getUserById(@PathVariable String id) {
        return userService.findUserById(id);
    }

}
