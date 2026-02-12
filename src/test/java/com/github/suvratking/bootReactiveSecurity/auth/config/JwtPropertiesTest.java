package com.github.suvratking.bootReactiveSecurity.auth.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtPropertiesTest {

    @Test
    void defaultValues_ShouldMatchExpectedDefaults() {
        JwtProperties properties = new JwtProperties();

        assertEquals("rzxlszyykpbgqcflzxsqcysyhljt", properties.getSecretKey());
        assertEquals(3600000L, properties.getValidityInMs());
    }

    @Test
    void setters_ShouldUpdateValues() {
        JwtProperties properties = new JwtProperties();

        properties.setSecretKey("custom-secret-key");
        properties.setValidityInMs(7200000L);

        assertEquals("custom-secret-key", properties.getSecretKey());
        assertEquals(7200000L, properties.getValidityInMs());
    }
}

