package com.bizkredit;

import com.bizkredit.config.JwtUtil;
import com.bizkredit.dto.LoginRequest;
import com.bizkredit.dto.RegisterRequest;
import com.bizkredit.entity.PasswordResetToken;
import com.bizkredit.entity.User;
import com.bizkredit.enums.Role;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.repository.AuditLogRepository;
import com.bizkredit.repository.PasswordResetTokenRepository;
import com.bizkredit.repository.UserRepository;
import com.bizkredit.service.AuthService;
import com.bizkredit.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Unit tests for AuthService - registration, login, JWT generation
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private PasswordResetTokenRepository resetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private CustomUserDetailsService customUserDetailsService;
    @Mock private UserDetails userDetails;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .userId(1L)
                .name("Harish Kumar")
                .email("harish@bizkredit.com")
                .password("hashedPassword")
                .phone("9876543210")
                .role(Role.ADMIN)
                .status("Active")
                .build();
    }

    @Test
    void register_success_returnsToken() {
        RegisterRequest request = new RegisterRequest(
                "Harish Kumar", "harish@bizkredit.com", "password123",
                "9876543210", Role.ADMIN, "BR001");

        when(userRepository.existsByEmail("harish@bizkredit.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(customUserDetailsService.loadUserByUsername("harish@bizkredit.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token-123");

        var response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token-123");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.email()).isEqualTo("harish@bizkredit.com");
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    void register_duplicateEmail_throwsBadRequest() {
        RegisterRequest request = new RegisterRequest(
                "Harish Kumar", "harish@bizkredit.com", "password123",
                "9876543210", Role.ADMIN, "BR001");

        when(userRepository.existsByEmail("harish@bizkredit.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success_returnsToken() {
        LoginRequest request = new LoginRequest("harish@bizkredit.com", "password123");

        when(userRepository.findByEmail("harish@bizkredit.com")).thenReturn(Optional.of(sampleUser));
        when(customUserDetailsService.loadUserByUsername("harish@bizkredit.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token-123");

        var response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token-123");
        assertThat(response.userId()).isEqualTo(1L);
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    void login_lockedAccount_throwsBadRequest() {
        sampleUser.setStatus("Locked");
        LoginRequest request = new LoginRequest("harish@bizkredit.com", "password123");

        when(userRepository.findByEmail("harish@bizkredit.com")).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Locked");
    }

    @Test
    void forgotPassword_devModeOn_knownEmail_returnsToken() {
        ReflectionTestUtils.setField(authService, "exposeResetTokenInResponse", true);
        when(userRepository.findByEmail("harish@bizkredit.com")).thenReturn(Optional.of(sampleUser));

        Optional<String> result = authService.forgotPassword("harish@bizkredit.com");

        assertThat(result).isPresent();
        verify(resetTokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    void forgotPassword_devModeOff_knownEmail_returnsEmptyButStillGeneratesToken() {
        ReflectionTestUtils.setField(authService, "exposeResetTokenInResponse", false);
        when(userRepository.findByEmail("harish@bizkredit.com")).thenReturn(Optional.of(sampleUser));

        Optional<String> result = authService.forgotPassword("harish@bizkredit.com");

        // Token is still generated and persisted server-side — just not handed back in the response
        assertThat(result).isEmpty();
        verify(resetTokenRepository, times(1)).save(any(PasswordResetToken.class));
    }

    @Test
    void forgotPassword_unknownEmail_returnsEmptyRegardlessOfDevMode() {
        ReflectionTestUtils.setField(authService, "exposeResetTokenInResponse", true);
        when(userRepository.findByEmail("nobody@bizkredit.com")).thenReturn(Optional.empty());

        Optional<String> result = authService.forgotPassword("nobody@bizkredit.com");

        // No account exists -> nothing generated, nothing returned (prevents enumeration)
        assertThat(result).isEmpty();
        verify(resetTokenRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }
}
