package org.example.bootReactiveSecurity.admin.service;

import lombok.RequiredArgsConstructor;
import org.example.bootReactiveSecurity.admin.dto.UserRequest;
import org.example.bootReactiveSecurity.admin.dto.UserResponse;
import org.example.bootReactiveSecurity.auth.entity.User;
import org.example.bootReactiveSecurity.auth.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<ResponseEntity<UserResponse>> findAll() {
        return userRepository
                .findAll()
                .collectList()
                .map(users ->  new ResponseEntity<>(new UserResponse(users), HttpStatus.OK));
    }

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

    public Mono<ResponseEntity<User>> findUserById(String id) {
        return userRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found with id: " + id)))
                .map(ResponseEntity::ok);
    }

}
