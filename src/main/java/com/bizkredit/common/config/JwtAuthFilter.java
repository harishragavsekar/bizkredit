package com.bizkredit.common.config;

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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    // Utility for JWT parsing and validation
    private final JwtUtil jwtUtil;

    // Service to load user details from DB
    private final CustomUserDetailsService userDetailsService;


     // This filter runs once per request and performs JWT authentication.

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Read Authorization header
        String authHeader = request.getHeader("Authorization");

        // If header is missing or not Bearer type, skip authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token (remove "Bearer " prefix)
        String jwt = authHeader.substring(7);

        String username;

        try {
            // Extract username from token
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            log.error("Invalid JWT token", e);
            filterChain.doFilter(request, response);
            return;
        }

        // Proceed only if user is not already authenticated
        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Load user from database
        var userDetails = userDetailsService.loadUserByUsername(username);

        // Validate token (username + expiration)
        if (!jwtUtil.isTokenValid(jwt, userDetails)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Create authentication token
        var authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        // Add request details (IP, session, etc.)
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Store authentication in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authToken);

        log.debug("JWT authenticated user: {}", username);

        // Continue request processing
        filterChain.doFilter(request, response);
    }
}
