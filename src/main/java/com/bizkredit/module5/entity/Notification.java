package com.bizkredit.module5.entity;

import com.bizkredit.module1.entity.User;
import com.bizkredit.common.enums.NotificationCategory;
import com.bizkredit.common.enums.NotificationStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationCategory category;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.UNREAD;

    @Builder.Default
    private LocalDate createdDate = LocalDate.now();
}