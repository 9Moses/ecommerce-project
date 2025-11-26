package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtDecoder jwtDecoder) {

        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/api/public/**",
                                "/api/product/public/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((exchange, ex) ->
                                writeUnauthorizedResponse(exchange.getResponse(), ex)
                        )
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoderWithFallback(jwtDecoder))
                                .jwtAuthenticationConverter(jwtAuthConverter())
                        )
                );

        return http.build();
    }

    /**
     * Unified fallback decoder. If Keycloak is down → return anonymous JWT.
     */
    private ReactiveJwtDecoder jwtDecoderWithFallback(ReactiveJwtDecoder mainDecoder) {
        return token -> mainDecoder.decode(token)
                .onErrorResume(ex -> {
                    if (ex instanceof WebClientRequestException ||
                            ex.getCause() instanceof ConnectException ||
                            ex.getMessage().contains("Connection refused")) {

                        System.out.println("⚠ Keycloak unreachable → switching to fallback anonymous user");

                        Jwt fallbackJwt = new Jwt(
                                "anonymous-token",
                                Instant.now(),
                                Instant.now().plusSeconds(3600),
                                Map.of("alg", "none"),
                                Map.of(
                                        "sub", "anonymous",
                                        "preferred_username", "anonymous",
                                        "roles", List.of("USER")
                                )
                        );

                        return Mono.just(fallbackJwt);
                    }

                    return Mono.error(new InvalidBearerTokenException("Invalid or expired token"));
                });
    }

    /**
     * Convert JWT → Spring Security Authentication
     */
    private Converter<Jwt, Mono<? extends AbstractAuthenticationToken>> jwtAuthConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthorityPrefix("ROLE_");
        roles.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = roles.convert(jwt);
            return authorities != null ? authorities : AuthorityUtils.NO_AUTHORITIES;
        });

        return jwt -> Mono.just(
                new JwtAuthenticationToken(jwt, converter.convert(jwt).getAuthorities())
        );
    }

    /**
     * Build consistent 401 JSON response
     */
    private Mono<Void> writeUnauthorizedResponse(
            org.springframework.http.server.reactive.ServerHttpResponse response,
            Exception ex) {

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "%s"
                }
                """.formatted(ex.getMessage());

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes()))
        );
    }
}
