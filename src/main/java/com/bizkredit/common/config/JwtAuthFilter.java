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

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Read Authorization header
        String authHeader = request.getHeader("Authorization");

        // Proceed only if Authorization header is present and starts with Bearer
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            // Extract JWT token by removing "Bearer " prefix
            String jwt = authHeader.substring(7);

            try {
                // Extract username from token
                String username = jwtUtil.extractUsername(jwt);

                // Authenticate only if username exists and no user is already authenticated
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Load user details from database
                    var userDetails = userDetailsService.loadUserByUsername(username);

                    // Validate token username and expiration
                    if (jwtUtil.isTokenValid(jwt, userDetails)) {

                        // Create authentication token
                        var authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                        // Add request details like IP address and session
                        authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // Store authentication in SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug("JWT authenticated user: {}", username);
                    }
                }

            } catch (Exception e) {
                // If token is invalid or expired, continue without authentication
                log.error("JWT authentication failed", e);
            }
        }

        // Continue request processing
        filterChain.doFilter(request, response);
    }
}