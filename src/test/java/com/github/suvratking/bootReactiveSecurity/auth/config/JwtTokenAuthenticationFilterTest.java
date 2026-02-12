package com.github.suvratking.bootReactiveSecurity.auth.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private WebFilterChain chain;

    private JwtTokenAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtTokenAuthenticationFilter(tokenProvider);
    }

    @Test
    void filter_ShouldBypassSwaggerPath() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/swagger-ui/index.html"));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void filter_ShouldBypassApiDocsPath() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/v3/api-docs"));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void filter_ShouldSkipAuth_WhenHeaderMissing() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/test"));
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void filter_ShouldSkipAuth_WhenTokenInvalid() {
        String token = "invalid-token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, JwtTokenAuthenticationFilter.HEADER_PREFIX + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(tokenProvider.validateToken(token)).thenReturn(false);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(tokenProvider).validateToken(token);
        verify(tokenProvider, never()).getAuthentication(token);
        verify(chain).filter(exchange);
    }

    @Test
    void filter_ShouldSkipAuth_WhenAuthorizationHeaderHasWrongPrefix() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDp0ZXN0")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(tokenProvider);
    }

    @Test
    void filter_ShouldSetAuthentication_WhenTokenValid() {
        String token = "valid-token";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, JwtTokenAuthenticationFilter.HEADER_PREFIX + token)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "n/a",
                List.of(() -> "ROLE_USER")
        );

        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getAuthentication(token)).thenReturn(authentication);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(tokenProvider).validateToken(token);
        verify(tokenProvider).getAuthentication(token);
        verify(chain).filter(exchange);
    }
}
