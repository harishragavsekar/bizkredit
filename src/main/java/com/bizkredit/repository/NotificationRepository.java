package com.bizkredit.repository;

import com.bizkredit.entity.Notification;
import com.bizkredit.enums.NotificationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<Notification> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    List<Notification> findByUser_UserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<Notification> findByStatus(NotificationStatus status);
}