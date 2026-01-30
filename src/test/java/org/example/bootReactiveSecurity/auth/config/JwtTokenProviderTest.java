package org.example.bootReactiveSecurity.auth.config;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        tokenProvider.init();
    }

    @Test
    void createToken_ShouldReturnValidToken() {
        Collection<? extends GrantedAuthority> authorities = 
                List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", 
                "password", 
                authorities
        );

        String token = tokenProvider.createToken(authentication);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void createToken_ShouldContainUsername() {
        Collection<? extends GrantedAuthority> authorities = 
                List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", 
                "password", 
                authorities
        );

        String token = tokenProvider.createToken(authentication);
        Authentication extractedAuth = tokenProvider.getAuthentication(token);

        assertEquals("testuser", extractedAuth.getName());
    }

    @Test
    void createToken_ShouldContainAuthorities() {
        Collection<? extends GrantedAuthority> authorities = 
                List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", 
                "password", 
                authorities
        );

        String token = tokenProvider.createToken(authentication);
        Authentication extractedAuth = tokenProvider.getAuthentication(token);

        assertTrue(extractedAuth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(extractedAuth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void createToken_WithoutAuthorities_ShouldCreateValidToken() {
        Collection<? extends GrantedAuthority> authorities = List.of();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", 
                "password", 
                authorities
        );

        String token = tokenProvider.createToken(authentication);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        Collection<? extends GrantedAuthority> authorities = 
                List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", 
                "password", 
                authorities
        );

        String token = tokenProvider.createToken(authentication);

        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        String invalidToken = "invalid.jwt.token";

        assertFalse(tokenProvider.validateToken(invalidToken));
    }

    @Test
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        String malformedToken = "malformed";

        assertFalse(tokenProvider.validateToken(malformedToken));
    }

    @Test
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        String emptyToken = "";

        assertFalse(tokenProvider.validateToken(emptyToken));
    }

    @Test
    void getAuthentication_ShouldExtractUsername() {
        String username = "testuser";
        Collection<? extends GrantedAuthority> authorities = 
                List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username, 
                "password", 
                authorities
        );

        String token = tokenProvider.createToken(authentication);
        Authentication extractedAuth = tokenProvider.getAuthentication(token);

        assertEquals(username, extractedAuth.getName());
    }

    @Test
    void getAuthentication_ShouldExtractAuthorities() {
        Collection<? extends GrantedAuthority> authorities = 
                List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", 
                "password", 
                authorities
        );

        String token = tokenProvider.createToken(authentication);
        Authentication extractedAuth = tokenProvider.getAuthentication(token);

        assertEquals(2, extractedAuth.getAuthorities().size());
        assertTrue(extractedAuth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(extractedAuth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void getAuthentication_WithMultipleAuthorities_ShouldParseCorrectly() {
        Collection<? extends GrantedAuthority> authorities = 
                List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_MANAGER")
                );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", 
                "password", 
                authorities
        );

        String token = tokenProvider.createToken(authentication);
        Authentication extractedAuth = tokenProvider.getAuthentication(token);

        assertEquals(3, extractedAuth.getAuthorities().size());
    }

    @Test
    void getAuthentication_WithInvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.token.here";

        assertThrows(JwtException.class, () -> tokenProvider.getAuthentication(invalidToken));
    }

    @Test
    void multipleTokens_ShouldBeDifferent() {
        Collection<? extends GrantedAuthority> authorities = 
                List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth1 = new UsernamePasswordAuthenticationToken(
                "user1", 
                "password", 
                authorities
        );
        Authentication auth2 = new UsernamePasswordAuthenticationToken(
                "user2", 
                "password", 
                authorities
        );

        String token1 = tokenProvider.createToken(auth1);
        String token2 = tokenProvider.createToken(auth2);

        assertNotEquals(token1, token2);
        assertEquals("user1", tokenProvider.getAuthentication(token1).getName());
        assertEquals("user2", tokenProvider.getAuthentication(token2).getName());
    }

}
