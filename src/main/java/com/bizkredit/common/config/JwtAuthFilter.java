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
// further requests , token based validation
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

         String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            // Extract JWT token by removing "Bearer " prefix
            String jwt = authHeader.substring(7);

            try {
                String username = jwtUtil.extractUsername(jwt);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    var userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtUtil.isTokenValid(jwt, userDetails)) {
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

                log.error("JWT authentication failed", e);
            }
        }

        // Continue request processing
        filterChain.doFilter(request, response);
    }
}