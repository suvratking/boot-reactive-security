package com.github.suvratking.bootReactiveSecurity.auth.config;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Web filter for JWT token authentication.
 * Intercepts requests to validate the JWT token and set the authentication context.
 */
@RequiredArgsConstructor
public class JwtTokenAuthenticationFilter implements WebFilter {

    public static final String HEADER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;

    /**
     * Filters incoming requests to check for a valid JWT token.
     * If a valid token is found, it extracts authentication details and sets the security context.
     *
     * @param exchange the current server web exchange.
     * @param chain    the web filter chain.
     * @return a {@link Mono} indicating when request processing is complete.
     */
    @Override
    @NullMarked
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var token = resolveToken(exchange.getRequest());
        if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) {
            return Mono.fromCallable(() -> this.tokenProvider.getAuthentication(token))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(authentication -> chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));
        }
        return chain.filter(exchange);
    }

    /**
     * Resolves the JWT token from the request headers.
     * Looks for the "Authorization" header starting with "Bearer ".
     *
     * @param request the server HTTP request.
     * @return the JWT token string if found, or {@code null} otherwise.
     */
    private String resolveToken(ServerHttpRequest request) {
        var bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(HEADER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

}
