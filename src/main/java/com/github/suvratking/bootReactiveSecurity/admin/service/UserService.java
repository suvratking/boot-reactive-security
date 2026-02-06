package com.github.suvratking.bootReactiveSecurity.admin.service;

import lombok.RequiredArgsConstructor;
import com.github.suvratking.bootReactiveSecurity.admin.dto.UserRequest;
import com.github.suvratking.bootReactiveSecurity.admin.dto.UserResponse;
import com.github.suvratking.bootReactiveSecurity.auth.entity.User;
import com.github.suvratking.bootReactiveSecurity.auth.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * Service class for managing user-related operations.
 * Handles business logic for creating, retrieving, updating, and deleting users.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves all users from the repository.
     *
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing a {@link UserResponse} with the list of all users.
     */
    public Mono<ResponseEntity<UserResponse>> findAll() {
        return userRepository
                .findAll()
                .collectList()
                .map(users -> new ResponseEntity<>(new UserResponse(users), HttpStatus.OK));
    }

    /**
     * Creates a new user based on the provided request.
     * Checks if a user with the given ID already exists. If so, returns a CONFLICT error.
     * Otherwise, encodes the password and saves the new user.
     *
     * @param userRequest a {@link Mono} containing the {@link UserRequest} with the new user's details.
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the created {@link User}.
     */
    public Mono<ResponseEntity<User>> createUser(Mono<UserRequest> userRequest) {
        return userRequest.flatMap(request ->
                        userRepository.findById(request.id())
                                .flatMap(_ -> Mono.<User>error(new ResponseStatusException(HttpStatus.CONFLICT, "User already exists")))
                                .switchIfEmpty(Mono.defer(() -> {
                                    User newUser = new User();
                                    BeanUtils.copyProperties(request, newUser);
                                    // example: encode password if needed
                                    newUser.setPassword(passwordEncoder.encode(request.password()));
                                    return userRepository.save(newUser);
                                }))
                )
                .map(ResponseEntity::ok);
    }

    /**
     * Updates an existing user identified by the given ID.
     *
     * @param userRequest a {@link Mono} containing the {@link UserRequest} with the updated details.
     * @param id          the unique identifier of the user to update.
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the updated {@link User}.
     * @throws RuntimeException if the user with the specified ID is not found.
     */
    public Mono<ResponseEntity<User>> updateUser(Mono<UserRequest> userRequest, String id) {
        return userRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found with id: " + id)))
                .zipWith(userRequest)
                .flatMap(tuple -> {
                    User existingUser = tuple.getT1();
                    UserRequest request = tuple.getT2();
                    BeanUtils.copyProperties(request, existingUser);
                    return userRepository.save(existingUser);
                })
                .map(ResponseEntity::ok);
    }

    /**
     * Deletes a user identified by the given ID.
     *
     * @param id the unique identifier of the user to delete.
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the deleted {@link User}.
     * @throws RuntimeException if the user with the specified ID is not found.
     */
    public Mono<ResponseEntity<User>> deleteUser(String id) {
        return userRepository
                .findById(id)
                .flatMap(existingUser ->
                        userRepository.delete(existingUser)
                                .then(Mono.just(existingUser))
                )
                .switchIfEmpty(Mono.error(new RuntimeException("User not found with id: " + id)))
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id the unique identifier of the user to retrieve.
     * @return a {@link Mono} emitting a {@link ResponseEntity} containing the found {@link User}.
     * @throws RuntimeException if the user with the specified ID is not found.
     */
    public Mono<ResponseEntity<User>> findUserById(String id) {
        return userRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found with id: " + id)))
                .map(ResponseEntity::ok);
    }

}
