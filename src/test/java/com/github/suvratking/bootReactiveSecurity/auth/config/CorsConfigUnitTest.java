package com.github.suvratking.bootReactiveSecurity.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.config.CorsRegistry;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigUnitTest {

    @Test
    @SuppressWarnings("unchecked")
    void addCorsMappings_ShouldRegisterExpectedConfiguration() throws Exception {
        CorsConfig corsConfig = new CorsConfig();
        CorsRegistry registry = new CorsRegistry();

        corsConfig.addCorsMappings(registry);

        Method method = CorsRegistry.class.getDeclaredMethod("getCorsConfigurations");
        method.setAccessible(true);
        Map<String, CorsConfiguration> configurations =
                (Map<String, CorsConfiguration>) method.invoke(registry);

        CorsConfiguration configuration = configurations.get("/**");
        assertNotNull(configuration);
        assertTrue(configuration.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(configuration.getAllowedMethods().contains("GET"));
        assertTrue(Boolean.TRUE.equals(configuration.getAllowCredentials()));
    }
}

