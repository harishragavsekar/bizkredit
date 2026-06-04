package com.bizkredit;

import com.bizkredit.entity.User;
import com.bizkredit.enums.Role;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.AuditLogRepository;
import com.bizkredit.repository.UserRepository;
import com.bizkredit.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .userId(1L)
                .name("Harish Kumar")
                .email("harish@bizkredit.com")
                .phone("9876543210")
                .role(Role.ADMIN)
                .status("Active")
                .build();
    }

    @Test
    void registerUser_success() {
        when(userRepository.existsByEmail(sampleUser.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        User result = userService.registerUser(sampleUser);

        assertThat(result.getEmail()).isEqualTo("harish@bizkredit.com");
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    void registerUser_duplicateEmail_throwsBadRequest() {
        when(userRepository.existsByEmail(sampleUser.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(sampleUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_notFound_throwsResourceNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateStatus_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        User updated = userService.updateStatus(1L, "Locked");

        assertThat(updated.getStatus()).isEqualTo("Locked");
        verify(auditLogRepository, times(1)).save(any());
    }
}
