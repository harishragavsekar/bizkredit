package com.bizkredit.service;

import com.bizkredit.entity.AuditLog;
import com.bizkredit.entity.User;
import com.bizkredit.enums.Role;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.AuditLogRepository;
import com.bizkredit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    // @Transactional(readOnly=true) - optimises DB reads, no write lock needed
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    // @Transactional - status update + audit log must succeed together
    @Transactional
    public User updateStatus(Long userId, String status) {
        User user = getUserById(userId);

        // Switch expression - validates status and throws if invalid
        String validStatus = switch (status) {
            case "Active", "Locked", "Inactive" -> status;
            default -> throw new BadRequestException(
                    "Invalid status. Must be Active, Locked or Inactive");
        };

        user.setStatus(validStatus);
        User updated = userRepository.save(user);
        log.info("User {} status changed to {}", userId, status);

        auditLogRepository.save(AuditLog.builder()
                .userId(userId)
                .action("STATUS_UPDATE:" + status)
                .entityType("User")
                .recordId(String.valueOf(userId))
                .build());

        return updated;
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogs(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }
}
