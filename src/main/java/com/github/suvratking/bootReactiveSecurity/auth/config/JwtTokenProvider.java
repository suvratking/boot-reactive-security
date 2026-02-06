package com.github.suvratking.bootReactiveSecurity.auth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;

import static java.util.stream.Collectors.joining;

/**
 * Component for generating and validating JWT tokens.
 * Handles token creation, authentication extraction, and validation.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final String key = "rzxlszyykpbgqcflzxsqcysyhljt";
    private final long validityInMs = 3600000; // 1h

    private static final String AUTHORITIES_KEY = "roles";

    private SecretKey secretKey;

    /**
     * Initializes the secret key for signing JWTs.
     * Encodes the raw key using Base64 and creates an HMAC SHA key.
     */
    @PostConstruct
    public void init() {
        var secret = Base64.getEncoder()
                .encodeToString(key.getBytes());
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a JWT token for the authenticated user.
     *
     * @param authentication the {@link Authentication} object containing user details and authorities.
     * @return a {@link String} representing the generated JWT token.
     */
    public String createToken(Authentication authentication) {

        var username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        var claimsBuilder = Jwts.claims().subject(username);
        if (!authorities.isEmpty()) {
            claimsBuilder.add(AUTHORITIES_KEY, authorities.stream()
                    .map(GrantedAuthority::getAuthority).collect(joining(",")));
        }

        var claims = claimsBuilder.build();

        var now = new Date();
        var validity = new Date(now.getTime() + validityInMs);

        return Jwts.builder().claims(claims).issuedAt(now).expiration(validity)
                .signWith(this.secretKey, Jwts.SIG.HS256).compact();

    }

    /**
     * Retrieves the authentication object from a given JWT token.
     * Parses the token to extract claims, including the subject (username) and authorities (roles).
     *
     * @param token the JWT token string.
     * @return an {@link Authentication} object representing the user and their authorities.
     */
    public Authentication getAuthentication(String token) {
        var claims = Jwts.parser().verifyWith(this.secretKey).build()
                .parseSignedClaims(token).getPayload();

        var authoritiesClaim = claims.get(AUTHORITIES_KEY);

        var authorities = authoritiesClaim == null
                ? AuthorityUtils.NO_AUTHORITIES
                : AuthorityUtils.commaSeparatedStringToAuthorityList(authoritiesClaim.toString());

        var principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * Validates the given JWT token.
     * Checks if the token is correctly signed and has not expired.
     *
     * @param token the JWT token string to validate.
     * @return {@code true} if the token is valid, {@code false} otherwise.
     */
    public boolean validateToken(String token) {
        try {
            var claims = Jwts.parser().verifyWith(this.secretKey)
                    .build().parseSignedClaims(token);
            // parseClaimsJws will check expiration date. No need do here.
            log.info("expiration date: {}", claims.getPayload().getExpiration());
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Invalid JWT token: {}", e.getMessage());
            log.trace("Invalid JWT token trace.", e);
        }
        return false;
    }
}
