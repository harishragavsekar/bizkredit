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

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(request.getRole())
                .branchId(request.getBranchId())
                .status("Active")
                .build();

        var saved = userRepository.save(user);
        log.info("User registered: {} [{}]", saved.getEmail(), saved.getRole());

        auditLogRepository.save(AuditLog.builder()
                .userId(saved.getUserId())
                .action("REGISTER")
                .entityType("User")
                .recordId(String.valueOf(saved.getUserId()))
                .build());

        var token = jwtUtil.generateToken(customUserDetailsService.loadUserByUsername(saved.getEmail()));
        return new AuthResponse(token, "Bearer", saved.getUserId(),
                saved.getName(), saved.getEmail(), saved.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!user.getStatus().equals("Active")) {
            throw new BadRequestException("Account is " + user.getStatus() + ". Contact admin.");
        }

        var token = jwtUtil.generateToken(customUserDetailsService.loadUserByUsername(user.getEmail()));
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
