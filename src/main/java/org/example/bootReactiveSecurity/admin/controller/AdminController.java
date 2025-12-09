package org.example.bootReactiveSecurity.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bootReactiveSecurity.admin.dto.UserRequest;
import org.example.bootReactiveSecurity.admin.dto.UserResponse;
import org.example.bootReactiveSecurity.admin.service.UserService;
import org.example.bootReactiveSecurity.auth.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/user")
    public Mono<ResponseEntity<UserResponse>> getUser() {
        return userService.findAll();
    }

    @PostMapping("/user")
    public Mono<ResponseEntity<User>> createUser(@Valid @RequestBody Mono<UserRequest> user) {
        return userService.createUser(user);
    }

    @PutMapping("/user/{id}")
    public Mono<ResponseEntity<User>> updateUser(@Valid @RequestBody Mono<UserRequest> user, @PathVariable String id) {
        return userService.updateUser(user, id);
    }

    @DeleteMapping("/user/{id}")
    public Mono<ResponseEntity<User>> deleteUser(@PathVariable String id) {
        return userService.deleteUser(id);
    }

    @GetMapping("/user/{id}")
    public Mono<ResponseEntity<User>> getUserById(@PathVariable String id) {
        return userService.findUserById(id);
    }

}
