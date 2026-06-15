package com.bizkredit.repository;

import com.bizkredit.entity.Notification;
import com.bizkredit.enums.NotificationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<Notification> findByUser_UserId(Long userId);

    List<Notification> findByStatus(NotificationStatus status);
}
