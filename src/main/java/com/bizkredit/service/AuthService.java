package com.bizkredit.service;

import com.bizkredit.config.JwtUtil;
import com.bizkredit.dto.AuthResponse;
import com.bizkredit.dto.LoginRequest;
import com.bizkredit.dto.RegisterRequest;
import com.bizkredit.entity.AuditLog;
import com.bizkredit.entity.User;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.repository.AuditLogRepository;
import com.bizkredit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// AuthService - handles user registration and login
// Uses Java 16+ Record DTOs - accessors like request.email() instead of request.getEmail()
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;

    // @Transactional - if anything fails, entire registration rolls back
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(request.role())
                .branchId(request.branchId())
                .status("Active")
                .build();

        User saved = userRepository.save(user);
        log.info("User registered: {} [{}]", saved.getEmail(), saved.getRole());

        auditLogRepository.save(AuditLog.builder()
                .userId(saved.getUserId())
                .action("REGISTER")
                .entityType("User")
                .recordId(String.valueOf(saved.getUserId()))
                .build());

        String token = jwtUtil.generateToken(
                customUserDetailsService.loadUserByUsername(saved.getEmail()));

        return new AuthResponse(token, "Bearer", saved.getUserId(),
                saved.getName(), saved.getEmail(), saved.getRole());
    }

    // @Transactional - audit log and login are atomic
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!user.getStatus().equals("Active")) {
            throw new BadRequestException("Account is " + user.getStatus() + ". Contact admin.");
        }

        String token = jwtUtil.generateToken(
                customUserDetailsService.loadUserByUsername(user.getEmail()));
        log.info("User logged in: {}", user.getEmail());

        auditLogRepository.save(AuditLog.builder()
                .userId(user.getUserId())
                .action("LOGIN")
                .entityType("User")
                .recordId(String.valueOf(user.getUserId()))
                .build());

        return new AuthResponse(token, "Bearer", user.getUserId(),
                user.getName(), user.getEmail(), user.getRole());
    }
}
