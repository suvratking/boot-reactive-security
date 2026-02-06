package org.example.bootReactiveSecurity.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<?>> handleValidation(WebExchangeBindException ex) {

        var errors = ex.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() == null ? "" : error.getDefaultMessage()
                ));

        return Mono.just(ResponseEntity.badRequest().body(errors));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Mono<ResponseEntity<?>> handleBadCredentialsException(BadCredentialsException ex) {
        return Mono.just(ResponseEntity.status(401).body(ex.getMessage()));
    }

}
