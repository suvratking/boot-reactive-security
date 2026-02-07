package com.github.suvratking.bootReactiveSecurity.admin.controller;

import com.github.suvratking.bootReactiveSecurity.admin.dto.UserRequest;
import com.github.suvratking.bootReactiveSecurity.admin.dto.UserResponse;
import com.github.suvratking.bootReactiveSecurity.admin.service.UserService;
import com.github.suvratking.bootReactiveSecurity.auth.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "Admin API", description = "The Admin API for user management")
public class AdminController {

    private final UserService userService;

    /**
     * Retrieves all users.
     *
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the {@link UserResponse} with the list of users.
     */
    @GetMapping("/user")
    @Operation(summary = "Get all users", description = "Retrieves a list of all users.")
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
    @Operation(summary = "Create a new user", description = "Creates a new user with the given details.")
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
    @Operation(summary = "Update an existing user", description = "Updates an existing user with the given details.")
    public Mono<ResponseEntity<User>> updateUser(@Valid @RequestBody Mono<UserRequest> user, @PathVariable Long id) {
        return userService.updateUser(user, id);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id the unique identifier of the user to be deleted.
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the deleted {@link User}.
     */
    @DeleteMapping("/user/{id}")
    @Operation(summary = "Delete a user", description = "Deletes a user by their unique identifier.")
    public Mono<ResponseEntity<User>> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id the unique identifier of the user to retrieve.
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the retrieved {@link User}.
     */
    @GetMapping("/user/{id}")
    @Operation(summary = "Get a user by ID", description = "Retrieves a user by their unique identifier.")
    public Mono<ResponseEntity<User>> getUserById(@PathVariable Long id) {
        return userService.findUserById(id);
    }

}
