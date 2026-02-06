package com.github.suvratking.bootReactiveSecurity.auth.repository;

import com.github.suvratking.bootReactiveSecurity.auth.entity.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface UserRepository extends ReactiveMongoRepository<User, Long> {
    Mono<User> findByUsername(String username);

    Mono<User> findTopByOrderByIdDesc();
}
