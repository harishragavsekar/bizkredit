package com.bizkredit.config;

import com.bizkredit.module1.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Validates JWT token authentication for every incoming request after login
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    // Utility class used to extract and validate JWT token data
    private final JwtUtil jwtUtil;

    // Loads user details from database using email/username
    private final CustomUserDetailsService userDetailsService;

    // Executes once for each HTTP request
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Read Authorization header from request
        final var authHeader = request.getHeader("Authorization");

        // Continue without authentication if token is missing or not a Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Remove "Bearer " prefix and get only the JWT token
        final var jwt = authHeader.substring(7);

        // Extract email/username from JWT subject
        final var userEmail = jwtUtil.extractEmail(jwt);

        // Authenticate request only if user is not already authenticated
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user details from database
            var userDetails = userDetailsService.loadUserByUsername(userEmail);

            // Validate token against user details and expiry
            if (jwtUtil.isTokenValid(jwt, userDetails)) {

                // Create Spring Security authentication object
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // Attach request-specific details like IP address and session info
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Store authenticated user in SecurityContext for this request
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // Log successful JWT authentication
                log.debug("JWT authenticated user: {}", userEmail);
            }
        }

        // Continue to the next filter/controller
        filterChain.doFilter(request, response);
    }
}