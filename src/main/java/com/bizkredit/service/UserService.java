package com.bizkredit.service;

import com.bizkredit.entity.AuditLog;
import com.bizkredit.entity.User;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.AuditLogRepository;
import com.bizkredit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BadRequestException("Email already registered: " + user.getEmail());
        }
        User saved = userRepository.save(user);
        log.info("New user registered: {} [{}]", saved.getEmail(), saved.getRole());
        auditLogRepository.save(AuditLog.builder()
                .userId(saved.getUserId())
                .action("REGISTER")
                .entityType("User")
                .recordId(String.valueOf(saved.getUserId()))
                .build());
        return saved;
    }

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

    public User updateStatus(Long userId, String status) {
        User user = getUserById(userId);
        user.setStatus(status);
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

    public List<AuditLog> getAuditLogs(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }
}
