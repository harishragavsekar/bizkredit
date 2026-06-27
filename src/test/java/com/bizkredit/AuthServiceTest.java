package com.bizkredit;

import com.bizkredit.config.JwtUtil;
import com.bizkredit.dto.LoginRequest;
import com.bizkredit.dto.RegisterRequest;
import com.bizkredit.entity.User;
import com.bizkredit.enums.Role;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ForbiddenException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private PasswordResetTokenRepository resetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private UserDetails userDetails;

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
                .branchId("BR001")
                .status("Active")
                .failedLoginAttempts(0)
                .build();
    }

    @Test
    void register_success_returnsToken() {
        RegisterRequest request = new RegisterRequest(
                "Harish Kumar",
                "harish@bizkredit.com",
                "password123",
                "9876543210",
                Role.ADMIN,
                "BR001"
        );

        when(userRepository.existsByEmail("harish@bizkredit.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(customUserDetailsService.loadUserByUsername("harish@bizkredit.com")).thenReturn(userDetails);
        when(jwtUtil.generateTokenWithClaims(any(UserDetails.class), anyMap())).thenReturn("jwt-token-123");

        var response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token-123");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Harish Kumar");
        assertThat(response.email()).isEqualTo("harish@bizkredit.com");
        assertThat(response.role()).isEqualTo(Role.ADMIN);

        verify(userRepository, times(1)).save(any(User.class));
        verify(auditLogRepository, times(1)).save(any());
        verify(customUserDetailsService, times(1)).loadUserByUsername("harish@bizkredit.com");
        verify(jwtUtil, times(1)).generateTokenWithClaims(any(UserDetails.class), anyMap());
    }

    @Test
    void register_duplicateEmail_throwsBadRequest() {
        RegisterRequest request = new RegisterRequest(
                "Harish Kumar",
                "harish@bizkredit.com",
                "password123",
                "9876543210",
                Role.ADMIN,
                "BR001"
        );

        when(userRepository.existsByEmail("harish@bizkredit.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any(User.class));
        verify(auditLogRepository, never()).save(any());
        verify(jwtUtil, never()).generateTokenWithClaims(any(UserDetails.class), anyMap());
    }

    @Test
    void login_success_returnsToken() {
        LoginRequest request = new LoginRequest(
                "harish@bizkredit.com",
                "password123"
        );

        when(userRepository.findByEmail("harish@bizkredit.com")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(customUserDetailsService.loadUserByUsername("harish@bizkredit.com")).thenReturn(userDetails);
        when(jwtUtil.generateTokenWithClaims(any(UserDetails.class), anyMap())).thenReturn("jwt-token-123");

        var response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token-123");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Harish Kumar");
        assertThat(response.email()).isEqualTo("harish@bizkredit.com");
        assertThat(response.role()).isEqualTo(Role.ADMIN);

        verify(authenticationManager, times(1)).authenticate(any());
        verify(userRepository, times(1)).save(sampleUser);
        verify(auditLogRepository, times(1)).save(any());
        verify(customUserDetailsService, times(1)).loadUserByUsername("harish@bizkredit.com");
        verify(jwtUtil, times(1)).generateTokenWithClaims(any(UserDetails.class), anyMap());
    }

    @Test
    void login_lockedAccount_throwsForbiddenException() {
        sampleUser.setStatus("Locked");

        LoginRequest request = new LoginRequest(
                "harish@bizkredit.com",
                "password123"
        );

        when(userRepository.findByEmail("harish@bizkredit.com")).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Locked");

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtUtil, never()).generateTokenWithClaims(any(UserDetails.class), anyMap());
    }

    @Test
    void forgotPassword_devModeOn_knownEmail_returnsToken() {
        ReflectionTestUtils.setField(authService, "exposeResetTokenInResponse", true);

        when(userRepository.findByEmail("harish@bizkredit.com")).thenReturn(Optional.of(sampleUser));

        Optional<String> result = authService.forgotPassword("harish@bizkredit.com");

        assertThat(result).isPresent();

        verify(resetTokenRepository, times(1)).deleteByUserId(1L);
        verify(resetTokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    void forgotPassword_devModeOff_knownEmail_returnsEmptyButStillGeneratesToken() {
        ReflectionTestUtils.setField(authService, "exposeResetTokenInResponse", false);

        when(userRepository.findByEmail("harish@bizkredit.com")).thenReturn(Optional.of(sampleUser));

        Optional<String> result = authService.forgotPassword("harish@bizkredit.com");

        assertThat(result).isEmpty();

        verify(resetTokenRepository, times(1)).deleteByUserId(1L);
        verify(resetTokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    void forgotPassword_unknownEmail_returnsEmptyRegardlessOfDevMode() {
        ReflectionTestUtils.setField(authService, "exposeResetTokenInResponse", true);

        when(userRepository.findByEmail("nobody@bizkredit.com")).thenReturn(Optional.empty());

        Optional<String> result = authService.forgotPassword("nobody@bizkredit.com");

        assertThat(result).isEmpty();

        verify(resetTokenRepository, never()).deleteByUserId(any());
        verify(resetTokenRepository, never()).save(any(PasswordResetToken.class));
        verify(auditLogRepository, never()).save(any());
    }
}