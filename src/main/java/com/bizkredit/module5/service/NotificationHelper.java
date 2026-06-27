package com.bizkredit.module5.service;

import com.bizkredit.module5.entity.Notification;
import com.bizkredit.common.enums.NotificationCategory;
import com.bizkredit.common.enums.NotificationStatus;
import com.bizkredit.module5.repository.NotificationRepository;
import com.bizkredit.module1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationHelper {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // Creates a notification if the userId maps to a valid user. Fails silently.
    public void notify(Long userId, String message, NotificationCategory category) {
        if (userId == null) return;
        try {
            userRepository.findById(userId).ifPresent(user ->
                notificationRepository.save(Notification.builder()
                        .user(user)
                        .message(message)
                        .category(category)
                        .status(NotificationStatus.UNREAD)
                        .createdDate(LocalDate.now())
                        .build())
            );
        } catch (Exception e) {
            log.warn("Failed to create notification for user {}: {}", userId, e.getMessage());
        }
    }
}
