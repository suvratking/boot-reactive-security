package com.github.suvratking.bootReactiveSecurity.auth.repository;

import com.github.suvratking.bootReactiveSecurity.auth.entity.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByUsername(String username);
}
