package org.example.bootReactiveSecurity.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.bootReactiveSecurity.admin.dto.UserRequest;
import org.example.bootReactiveSecurity.auth.entity.User;
import org.example.bootReactiveSecurity.auth.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<ResponseEntity<User>> register(Mono<UserRequest> userRequest) {
        return userRequest
                .flatMap(req -> userRepository
                .findById(req.id())
                .flatMap(_ -> Mono.<User>error(new RuntimeException("User already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    User newUser = new User();
                    BeanUtils.copyProperties(req, newUser);
                    newUser.setPassword(passwordEncoder.encode(req.password()));
                    return userRepository.save(newUser);
                })))
                .map(ResponseEntity::ok);
    }

}
