package com.bizkredit.module1;

import com.bizkredit.module1.entity.User;
import com.bizkredit.module1.repository.AuditLogRepository;
import com.bizkredit.module1.repository.UserRepository;
import com.bizkredit.module1.service.UserService;
import com.bizkredit.common.enums.Role;
import com.bizkredit.common.exception.BadRequestException;
import com.bizkredit.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AuditLogRepository auditLogRepository;

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
                .password("hashedpassword")
                .role(Role.ADMIN)
                .status("Active")
                .build();
    }

    @Test
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        var result = userService.getUserById(1L);

        assertThat(result.getEmail()).isEqualTo("harish@bizkredit.com");
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

        var updated = userService.updateStatus(1L, "Locked");

        assertThat(updated.getStatus()).isEqualTo("Locked");
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    void updateStatus_invalidStatus_throwsBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> userService.updateStatus(1L, "InvalidStatus"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid status");
    }
}
