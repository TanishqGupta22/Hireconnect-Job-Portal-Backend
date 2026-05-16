package com.hireconnect.gateway.filter;

import com.hireconnect.gateway.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class JwtAuthGatewayFilter extends AbstractGatewayFilterFactory<JwtAuthGatewayFilter.Config> {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/login",
            "/auth/register", 
            "/auth/refresh",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/auth/validate",
            "/auth/oauth2",
            "/actuator/**"
    );

    private static final List<String> NON_AUTH_ROUTES = List.of(
            "/jobs",
            "/profiles",
            "/applications",
            "/interviews",
            "/notifications",
            "/subscriptions",
            "/invoices",
            "/analytics"
    );
    
    @Autowired
    private JwtService jwtService;

    public JwtAuthGatewayFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            HttpMethod method = request.getMethod();

            log.info("Gateway processing request: {} {}", method, path);

            // Allow public paths without authentication
            if (isPublicPath(path, method)) {
                log.info("Public path allowed: {}", path);
                return chain.filter(exchange);
            }

            // Check for Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {}", path);
                // Allow non-auth routes to proceed without auth
                if (NON_AUTH_ROUTES.stream().anyMatch(path::startsWith)) {
                    log.info("Allowing non-auth route without auth: {}", path);
                    return chain.filter(exchange);
                }
                return handleUnauthorized(exchange.getResponse());
            }

            // Extract and validate JWT token locally
            String token = authHeader.substring(7);
            log.debug("Validating JWT token for path: {}", path);
            
            try {
                if (jwtService == null) {
                    log.error("JwtService is null - this should not happen!");
                    // Allow non-auth routes to proceed
                    if (NON_AUTH_ROUTES.stream().anyMatch(path::startsWith)) {
                        return chain.filter(exchange);
                    }
                    return handleUnauthorized(exchange.getResponse());
                }
                
                String username = jwtService.extractUsername(token);
                if (jwtService.validateToken(token, username)) {
                    log.info("JWT token validated successfully for user: {}", username);
                    // Add user info to headers for downstream services
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Email", username)
                            .header("X-User-Name", username)
                            .build();
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                } else {
                    log.warn("JWT token validation failed for user: {}", username);
                    // Allow non-auth routes to proceed
                    if (NON_AUTH_ROUTES.stream().anyMatch(path::startsWith)) {
                        return chain.filter(exchange);
                    }
                    return handleUnauthorized(exchange.getResponse());
                }
            } catch (io.jsonwebtoken.JwtException e) {
                log.warn("JWT token validation failed for path {}: {}", path, e.getMessage());
                // Don't block non-auth routes on JWT errors
                if (NON_AUTH_ROUTES.stream().anyMatch(path::startsWith)) {
                    log.info("Allowing non-auth route to proceed despite JWT error: {}", path);
                    return chain.filter(exchange);
                }
                return handleUnauthorized(exchange.getResponse());
            } catch (SecurityException e) {
                log.warn("Security exception for path {}: {}", path, e.getMessage());
                // Don't block non-auth routes on security errors
                if (NON_AUTH_ROUTES.stream().anyMatch(path::startsWith)) {
                    log.info("Allowing non-auth route to proceed despite security error: {}", path);
                    return chain.filter(exchange);
                }
                return handleUnauthorized(exchange.getResponse());
            } catch (Exception e) {
                log.error("Unexpected error during JWT validation for path {}: {}", path, e.getMessage(), e);
                // Don't block non-auth routes on unexpected errors
                if (NON_AUTH_ROUTES.stream().anyMatch(path::startsWith)) {
                    log.info("Allowing non-auth route to proceed despite unexpected error: {}", path);
                    return chain.filter(exchange);
                }
                return handleUnauthorized(exchange.getResponse());
            }
        };
    }

    private boolean isPublicPath(String path, HttpMethod method) {
        if (HttpMethod.OPTIONS.equals(method)) {
            return true;
        }
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith) || 
               NON_AUTH_ROUTES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> handleUnauthorized(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = "{\"timestamp\":\"" + java.time.Instant.now().toString() + 
                     "\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}";
        return response.writeWith(
            Mono.just(response.bufferFactory().wrap(body.getBytes()))
        );
    }

    public static class Config {
    }
}
