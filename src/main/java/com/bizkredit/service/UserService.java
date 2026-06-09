package com.bizkredit.service;

import com.bizkredit.entity.AuditLog;
import com.bizkredit.entity.User;
import com.bizkredit.enums.Role;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.AuditLogRepository;
import com.bizkredit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

// UserService - user management operations
// Uses Java 21 features: var, switch expressions
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    // Update user status with audit logging
    public User updateStatus(Long userId, String status) {
        var user = getUserById(userId);

        // Java 14+ switch expression - validates status value
        var validStatus = switch (status) {
            case "Active", "Locked", "Inactive" -> status;
            default -> throw new com.bizkredit.exception.BadRequestException(
                    "Invalid status. Must be Active, Locked or Inactive");
        };

        user.setStatus(validStatus);
        var updated = userRepository.save(user);
        log.info("User {} status changed to {}", userId, status);

        auditLogRepository.save(AuditLog.builder()
                .userId(userId)
                .action("STATUS_UPDATE:" + status)
                .entityType("User")
                .recordId(String.valueOf(userId))
                .build());

        return updated;
    }

    public List<AuditLog> getAuditLogs(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }
}
