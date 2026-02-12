package com.github.suvratking.bootReactiveSecurity.auth.config;

import com.github.suvratking.bootReactiveSecurity.auth.entity.User;
import com.github.suvratking.bootReactiveSecurity.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigurationTest {

    @Mock
    private UserRepository userRepository;

    private final SecurityConfiguration securityConfiguration = new SecurityConfiguration();

    @Test
    void passwordEncoder_ShouldBeCreated() {
        PasswordEncoder passwordEncoder = securityConfiguration.passwordEncoder();

        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder.matches("password", passwordEncoder.encode("password")));
    }

    @Test
    void userDetailsService_ShouldMapRepositoryUser() {
        User user = User.builder()
                .id(1L)
                .username("alice")
                .password("{noop}password")
                .active(true)
                .roles(List.of("ROLE_USER"))
                .build();
        when(userRepository.findByUsername("alice")).thenReturn(Mono.just(user));

        ReactiveUserDetailsService service = securityConfiguration.userDetailsService(userRepository);

        StepVerifier.create(service.findByUsername("alice"))
                .assertNext(details -> {
                    assertTrue(details.isEnabled());
                    assertTrue(details.getAuthorities().stream()
                            .anyMatch(authority -> "ROLE_USER".equals(authority.getAuthority())));
                })
                .verifyComplete();
    }

    @Test
    void userDetailsService_ShouldDisableInactiveUser() {
        User user = User.builder()
                .id(2L)
                .username("inactive-user")
                .password("{noop}password")
                .active(false)
                .roles(List.of("ROLE_USER"))
                .build();
        when(userRepository.findByUsername("inactive-user")).thenReturn(Mono.just(user));

        ReactiveUserDetailsService service = securityConfiguration.userDetailsService(userRepository);

        StepVerifier.create(service.findByUsername("inactive-user"))
                .assertNext(details -> {
                    assertTrue(!details.isAccountNonExpired());
                    assertTrue(!details.isCredentialsNonExpired());
                    assertTrue(!details.isEnabled());
                    assertTrue(!details.isAccountNonLocked());
                })
                .verifyComplete();
    }

    @Test
    void reactiveAuthenticationManager_ShouldAuthenticateValidCredentials() {
        PasswordEncoder passwordEncoder = securityConfiguration.passwordEncoder();
        UserDetails details = org.springframework.security.core.userdetails.User.withUsername("alice")
                .password(passwordEncoder.encode("password"))
                .authorities("ROLE_USER")
                .build();
        ReactiveUserDetailsService userDetailsService = username -> Mono.just(details);

        ReactiveAuthenticationManager manager = securityConfiguration
                .reactiveAuthenticationManager(userDetailsService, passwordEncoder);

        StepVerifier.create(manager.authenticate(new UsernamePasswordAuthenticationToken("alice", "password")))
                .assertNext(Authentication::isAuthenticated)
                .verifyComplete();
    }

    @Test
    void corsConfigurationSource_ShouldContainExpectedRules() {
        CorsConfigurationSource source = securityConfiguration.corsConfigurationSource();
        CorsConfiguration configuration = source.getCorsConfiguration(
                MockServerWebExchange.from(MockServerHttpRequest.get("/any-path"))
        );

        assertNotNull(configuration);
        assertTrue(configuration.getAllowedOriginPatterns().contains("*"));
        assertTrue(configuration.getAllowedMethods().contains("GET"));
        assertTrue(configuration.getExposedHeaders().contains("Authorization"));
        assertTrue(Boolean.TRUE.equals(configuration.getAllowCredentials()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void currentUserMatchesPath_ShouldGrantWhenUserMatches() throws Exception {
        Method method = SecurityConfiguration.class.getDeclaredMethod(
                "currentUserMatchesPath",
                Mono.class,
                AuthorizationContext.class
        );
        method.setAccessible(true);

        AuthorizationContext context = new AuthorizationContext(
                MockServerWebExchange.from(MockServerHttpRequest.get("/admin/user/alice")),
                Map.of("user", "alice")
        );
        Mono<AuthorizationDecision> result = (Mono<AuthorizationDecision>) method.invoke(
                securityConfiguration,
                Mono.just(new UsernamePasswordAuthenticationToken("alice", "n/a")),
                context
        );

        StepVerifier.create(result)
                .assertNext(AuthorizationDecision::isGranted)
                .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void currentUserMatchesPath_ShouldDenyWhenUserDoesNotMatch() throws Exception {
        Method method = SecurityConfiguration.class.getDeclaredMethod(
                "currentUserMatchesPath",
                Mono.class,
                AuthorizationContext.class
        );
        method.setAccessible(true);

        AuthorizationContext context = new AuthorizationContext(
                MockServerWebExchange.from(MockServerHttpRequest.get("/admin/user/alice")),
                Map.of("user", "alice")
        );
        Mono<AuthorizationDecision> result = (Mono<AuthorizationDecision>) method.invoke(
                securityConfiguration,
                Mono.just(new UsernamePasswordAuthenticationToken("bob", "n/a")),
                context
        );

        StepVerifier.create(result)
                .assertNext(decision -> assertTrue(!decision.isGranted()))
                .verifyComplete();
    }

    @Test
    void springWebFilterChain_ShouldBuildSuccessfully() {
        ReactiveAuthenticationManager authenticationManager = authentication -> Mono.just(authentication);
        JwtTokenProvider tokenProvider = new JwtTokenProvider();
        tokenProvider.init();

        SecurityWebFilterChain filterChain = securityConfiguration.springWebFilterChain(
                ServerHttpSecurity.http(),
                tokenProvider,
                authenticationManager
        );

        assertNotNull(filterChain);
    }
}
