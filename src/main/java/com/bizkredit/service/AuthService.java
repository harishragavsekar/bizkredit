package com.bizkredit.service;

import com.bizkredit.config.JwtUtil;
import com.bizkredit.dto.AuthResponse;
import com.bizkredit.dto.LoginRequest;
import com.bizkredit.dto.RegisterRequest;
import com.bizkredit.entity.AuditLog;
import com.bizkredit.entity.PasswordResetToken;
import com.bizkredit.entity.User;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ForbiddenException;
import com.bizkredit.repository.AuditLogRepository;
import com.bizkredit.repository.PasswordResetTokenRepository;
import com.bizkredit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Pattern PASSWORD_POLICY = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$"
    );

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
                .failedLoginAttempts(0)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered: {} [{}]", saved.getEmail(), saved.getRole());

        auditLogRepository.save(AuditLog.builder()
                .userId(saved.getUserId())
                .action("REGISTER")
                .entityType("User")
                .recordId(String.valueOf(saved.getUserId()))
                .build());

        String token = buildToken(saved);
        return new AuthResponse(token, "Bearer", saved.getUserId(),
                saved.getName(), saved.getEmail(), saved.getRole());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Attempt authentication — BadCredentialsException maps to 401 via GlobalExceptionHandler
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException e) {
            // Increment failed attempts and potentially lock account
            userRepository.findByEmail(request.email()).ifPresent(u -> {
                int attempts = (u.getFailedLoginAttempts() == null ? 0 : u.getFailedLoginAttempts()) + 1;
                u.setFailedLoginAttempts(attempts);
                if (attempts >= MAX_FAILED_ATTEMPTS) {
                    u.setStatus("Locked");
                    log.warn("Account locked after {} failed attempts: {}", attempts, u.getEmail());
                }
                userRepository.save(u);
                auditLogRepository.save(AuditLog.builder()
                        .userId(u.getUserId())
                        .action("LOGIN_FAILED")
                        .entityType("User")
                        .recordId(String.valueOf(u.getUserId()))
                        .build());
            });
            throw e; // rethrow — GlobalExceptionHandler maps BadCredentialsException to 401
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        // Check account status — return 403 for locked/inactive
        if (!user.getStatus().equals("Active")) {
            throw new ForbiddenException("Account is " + user.getStatus() + ". Contact admin.");
        }

        // Reset failed attempts on successful login
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        String token = buildToken(user);
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

    // Stateless logout — token invalidation is client-side; this endpoint logs the action
    @Transactional
    public void logout(Long userId) {
        auditLogRepository.save(AuditLog.builder()
                .userId(userId)
                .action("LOGOUT")
                .entityType("User")
                .recordId(String.valueOf(userId))
                .build());
        log.info("User logged out: {}", userId);
    }

    // Step 1 of password reset — generate token and send via in-app notification
    // Returns 200 regardless of whether email exists (prevents enumeration)
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // Delete any existing unused tokens for this user
            resetTokenRepository.deleteByUserId(user.getUserId());

            String token = UUID.randomUUID().toString();
            resetTokenRepository.save(PasswordResetToken.builder()
                    .token(token)
                    .userId(user.getUserId())
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .used(false)
                    .build());

            // In Phase 1 — log the reset token; in Phase 2 this would send email/notification
            log.info("Password reset token generated for user {}: {} (expires in 15 min)",
                    user.getEmail(), token);

            auditLogRepository.save(AuditLog.builder()
                    .userId(user.getUserId())
                    .action("PASSWORD_RESET_REQUESTED")
                    .entityType("User")
                    .recordId(String.valueOf(user.getUserId()))
                    .build());
        });
    }

    // Step 2 of password reset — validate token and set new password
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("Reset token has already been used");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        if (!PASSWORD_POLICY.matcher(newPassword).matches()) {
            throw new BadRequestException(
                    "Password must be at least 8 characters with uppercase, lowercase, digit, and special character");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        // Unlock account on successful reset
        if ("Locked".equals(user.getStatus())) {
            user.setStatus("Active");
            user.setFailedLoginAttempts(0);
        }
        userRepository.save(user);

        // Mark token as used (single-use)
        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);

        auditLogRepository.save(AuditLog.builder()
                .userId(user.getUserId())
                .action("PASSWORD_RESET")
                .entityType("User")
                .recordId(String.valueOf(user.getUserId()))
                .build());

        log.info("Password reset successfully for user {}", user.getEmail());
    }

    // Builds JWT with userId, role, branchId claims
    private String buildToken(User user) {
        var userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("role", user.getRole().name());
        claims.put("branchId", user.getBranchId());
        return jwtUtil.generateTokenWithClaims(userDetails, claims);
    }
}
