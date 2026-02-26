package com.ecommerce.cartservice.config;

import com.ecommerce.cartservice.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        log.info("=== JWT Filter Debug ===");
        log.info("Request URI: {}", request.getRequestURI());
        log.info("Auth Header: {}", authHeader != null ? "Present" : "Missing");

        // Check if Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No valid Authorization header found");
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token
        jwt = authHeader.substring(7);
        log.info("JWT Token extracted (first 20 chars): {}", jwt.substring(0, Math.min(20, jwt.length())));


        try {
            // Validate and extract user information from JWT
            if (jwtUtil.validateToken(jwt)) {
                log.info("JWT Token is VALID");

                userEmail = jwtUtil.extractUsername(jwt);
                log.info("Extracted username: {}", userEmail);
                
                // If username is present and no authentication is set in context
                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Extract role from JWT token
                    String role = jwtUtil.extractRole(jwt);
                    log.info("Extracted role from token: {}", role);

                    // Default to USER if role is not present
                    if (role == null || role.isEmpty()) {
                        role = "USER";
                        log.warn("Role was null/empty, defaulting to USER");
                    }

                    // Create authority with ROLE_ prefix
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                    log.info("Created authority: {}", authority.getAuthority());

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail,
                            null,
                            Collections.singletonList(authority)
                    );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("JWT token validated for user: {}", userEmail);
                    log.info("Authentication set in SecurityContext for user: {} with role: {}", userEmail, role);
                }else {
                    log.warn("Username is null or authentication already exists");
                }
            } else {
                log.error("JWT Token validation FAILED");
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        log.info("=== End JWT Filter Debug ===");
        filterChain.doFilter(request, response);
    }
}
