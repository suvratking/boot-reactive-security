package com.github.suvratking.bootReactiveSecurity.auth.config;

import com.github.suvratking.bootReactiveSecurity.auth.repository.UserRepository;
import com.github.suvratking.bootReactiveSecurity.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class CorsConfigTest {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    private final ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/"));

    @Test
    void corsConfigurationSource_ShouldBeConfigured() {
        assertNotNull(corsConfigurationSource);
    }

    @Test
    void corsConfiguration_ShouldAllowAllOrigins() {
        CorsConfiguration corsConfig = corsConfigurationSource.getCorsConfiguration(exchange);

        assertNotNull(corsConfig);
        assertNotNull(corsConfig.getAllowedOriginPatterns());
        assertTrue(corsConfig.getAllowedOriginPatterns().contains("*"));
    }

    @Test
    void corsConfiguration_ShouldAllowAllHttpMethods() {
        CorsConfiguration corsConfig = corsConfigurationSource.getCorsConfiguration(exchange);

        assertNotNull(corsConfig);
        assertNotNull(corsConfig.getAllowedMethods());
        assertTrue(corsConfig.getAllowedMethods().contains("GET"));
        assertTrue(corsConfig.getAllowedMethods().contains("POST"));
        assertTrue(corsConfig.getAllowedMethods().contains("PUT"));
        assertTrue(corsConfig.getAllowedMethods().contains("DELETE"));
        assertTrue(corsConfig.getAllowedMethods().contains("PATCH"));
        assertTrue(corsConfig.getAllowedMethods().contains("OPTIONS"));
    }

    @Test
    void corsConfiguration_ShouldAllowAllHeaders() {
        CorsConfiguration corsConfig = corsConfigurationSource.getCorsConfiguration(exchange);

        assertNotNull(corsConfig);
        assertNotNull(corsConfig.getAllowedHeaders());
        assertTrue(corsConfig.getAllowedHeaders().contains("*"));
    }

    @Test
    void corsConfiguration_ShouldExposeAuthorizationHeader() {
        CorsConfiguration corsConfig = corsConfigurationSource.getCorsConfiguration(exchange);

        assertNotNull(corsConfig);
        assertNotNull(corsConfig.getExposedHeaders());
        assertTrue(corsConfig.getExposedHeaders().contains("Authorization"));
    }

    @Test
    void corsConfiguration_ShouldAllowCredentials() {
        CorsConfiguration corsConfig = corsConfigurationSource.getCorsConfiguration(exchange);

        assertNotNull(corsConfig);
        assertTrue(corsConfig.getAllowCredentials());
    }

    @Test
    void corsConfiguration_ShouldHaveMaxAge() {
        CorsConfiguration corsConfig = corsConfigurationSource.getCorsConfiguration(exchange);

        assertNotNull(corsConfig);
        assertEquals(3600L, corsConfig.getMaxAge());
    }

}
