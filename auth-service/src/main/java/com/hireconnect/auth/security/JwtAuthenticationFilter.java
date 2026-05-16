package com.hireconnect.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/") || 
               path.startsWith("/api/auth/") ||
               path.startsWith("/login") ||
               path.startsWith("/oauth2/") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars") ||
               path.startsWith("/error") ||
               path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        log.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        log.debug("Authorization header: {}", authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No valid Authorization header found, skipping JWT filter");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        log.debug("Extracted JWT token (first 20 chars): {}", jwt.substring(0, Math.min(20, jwt.length())));
        
        try {
            userEmail = jwtService.extractUsername(jwt);
            log.debug("Extracted username from token: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to extract username from JWT: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.debug("User email extracted and no existing authentication, loading user details");
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                log.debug("User details loaded for: {}", userDetails.getUsername());

                if (jwtService.validateToken(jwt, userDetails.getUsername())) {
                    log.debug("JWT token validation successful for user: {}", userEmail);
                    log.debug("User authorities: {}", userDetails.getAuthorities());
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Authentication set successfully for user: {} with authorities: {}", userEmail, userDetails.getAuthorities());
                } else {
                    log.warn("JWT token validation failed for user: {}", userEmail);
                }
            } catch (Exception e) {
                log.error("Error loading user details or validating token: {}", e.getMessage());
            }
        } else {
            log.debug("User email is null or authentication already exists");
        }

        filterChain.doFilter(request, response);
    }
}
