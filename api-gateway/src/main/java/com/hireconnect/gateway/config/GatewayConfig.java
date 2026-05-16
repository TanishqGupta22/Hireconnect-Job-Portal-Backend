package com.hireconnect.gateway.config;

import com.hireconnect.gateway.filter.JwtAuthGatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, JwtAuthGatewayFilter jwtFilter) {
        return builder.routes()
                // Auth Service Routes (Public + Protected)
                .route("auth-service", r -> r.path("/auth/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthGatewayFilter.Config())))
                        .uri("http://localhost:8081"))
                
                // Profile Service Routes (Protected)
                .route("profile-service", r -> r.path("/profiles/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthGatewayFilter.Config())))
                        .uri("http://localhost:8082"))
                
                // Job Service Routes (Public + Protected)
                .route("job-service", r -> r.path("/jobs/**")
                        .uri("http://localhost:8083"))
                
                // Application Service Routes (Protected)
                .route("application-service", r -> r.path("/applications/**")
                        .uri("http://localhost:8084"))
                
                // Interview Service Routes (Protected)
                .route("interview-service", r -> r.path("/interviews/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthGatewayFilter.Config())))
                        .uri("http://localhost:8085"))
                
                // Notification Service Routes (Protected)
                .route("notification-service", r -> r.path("/notifications/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthGatewayFilter.Config())))
                        .uri("http://localhost:8086"))
                
                // Subscription Service Routes (Protected)
                .route("subscription-service", r -> r.path("/subscriptions/**", "/invoices/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthGatewayFilter.Config())))
                        .uri("http://localhost:8087"))
                
                // Analytics Service Routes (Protected)
                .route("analytics-service", r -> r.path("/analytics/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthGatewayFilter.Config())))
                        .uri("http://localhost:8088"))
                
                .build();
    }
}
