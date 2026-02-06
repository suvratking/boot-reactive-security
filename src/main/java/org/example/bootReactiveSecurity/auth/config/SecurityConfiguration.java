package org.example.bootReactiveSecurity.auth.config;

import org.example.bootReactiveSecurity.auth.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Configuration class for security settings.
 * Configures authentication, authorization, CORS, and password encoding.
 */
@Configuration
public class SecurityConfiguration {

    /**
     * Creates a password encoder bean.
     * Uses delegating password encoder to support multiple encoding formats.
     *
     * @return a {@link PasswordEncoder} instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Configures the security filter chain.
     * Sets up CORS, CSRF, authentication manager, security context repository, and authorization rules.
     * Adds the JWT token authentication filter.
     *
     * @param http                        the {@link ServerHttpSecurity} to configure.
     * @param tokenProvider               the {@link JwtTokenProvider} for JWT operations.
     * @param reactiveAuthenticationManager the {@link ReactiveAuthenticationManager} for authentication.
     * @return a {@link SecurityWebFilterChain} defining the security filter chain.
     */
    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http,
                                                JwtTokenProvider tokenProvider,
                                                ReactiveAuthenticationManager reactiveAuthenticationManager) {
        final String WHITE_LIST_PATH = "/auth/**";

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authenticationManager(reactiveAuthenticationManager)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .pathMatchers(WHITE_LIST_PATH).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(new JwtTokenAuthenticationFilter(tokenProvider), SecurityWebFiltersOrder.HTTP_BASIC)
                .build();


    }

    /**
     * Checks if the current user matches the user ID in the path variable.
     *
     * @param authentication the current authentication.
     * @param context        the authorization context containing path variables.
     * @return a {@link Mono} emitting an {@link AuthorizationDecision}.
     */
    private Mono<AuthorizationDecision> currentUserMatchesPath(Mono<Authentication> authentication,
                                                               AuthorizationContext context) {
        return authentication
                .map(a -> context.getVariables().get("user").equals(a.getName()))
                .map(AuthorizationDecision::new);

    }

    /**
     * Creates a reactive user details service bean.
     * Loads user details from the repository and converts them to Spring Security's User object.
     *
     * @param users the {@link UserRepository} to retrieve user data.
     * @return a {@link ReactiveUserDetailsService} instance.
     */
    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository users) {

        return username -> users.findByUsername(username)
                .map(u -> User
                        .withUsername(u.getUsername()).password(u.getPassword())
                        .authorities(u.getRoles().toArray(new String[0]))
                        .accountExpired(!u.isActive())
                        .credentialsExpired(!u.isActive())
                        .disabled(!u.isActive())
                        .accountLocked(!u.isActive())
                        .build()
                );
    }

    /**
     * Creates a reactive authentication manager bean.
     * Configures it with the user details service and password encoder.
     *
     * @param userDetailsService the {@link ReactiveUserDetailsService} to load user data.
     * @param passwordEncoder    the {@link PasswordEncoder} to verify passwords.
     * @return a {@link ReactiveAuthenticationManager} instance.
     */
    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(ReactiveUserDetailsService userDetailsService,
                                                                       PasswordEncoder passwordEncoder) {
        var authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder);
        return authenticationManager;
    }

    /**
     * Configures CORS settings.
     * Allows all origins, methods, and headers, and enables credentials.
     *
     * @return a {@link CorsConfigurationSource} instance.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();

        // FULL support: allow all origins + credentials
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));  // optional
        config.setAllowCredentials(true); // credentials + dynamic origins
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}
