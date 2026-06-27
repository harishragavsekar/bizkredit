package com.bizkredit.service;

import com.bizkredit.entity.*;
import com.bizkredit.enums.CovenantStatus;
import com.bizkredit.enums.EWSStatus;
import com.bizkredit.enums.NotificationCategory;
import com.bizkredit.enums.NotificationStatus;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.CovenantRepository;
import com.bizkredit.repository.EarlyWarningSignalRepository;
import com.bizkredit.repository.FacilityAccountRepository;
import com.bizkredit.repository.NotificationRepository;
import com.bizkredit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CovenantNotificationService {

    private final CovenantRepository covenantRepository;
    private final EarlyWarningSignalRepository ewsRepository;
    private final NotificationRepository notificationRepository;
    private final FacilityAccountRepository facilityRepository;
    private final UserRepository userRepository;

    @Transactional
    public Covenant createCovenant(Long facilityId, Covenant covenant) {
        FacilityAccount facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found"));

        covenant.setFacility(facility);
        covenant.setStatus(CovenantStatus.ACTIVE);

        return covenantRepository.save(covenant);
    }

    @Transactional(readOnly = true)
    public List<Covenant> getCovenantsByFacility(Long facilityId) {
        return covenantRepository.findByFacility_FacilityId(facilityId);
    }

    @Transactional
    public Covenant updateCovenant(Long covenantId, Covenant updates) {
        Covenant covenant = covenantRepository.findById(covenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Covenant not found"));

        if (updates.getDescription() != null) {
            covenant.setDescription(updates.getDescription());
        }

        if (updates.getThresholdValue() != null) {
            covenant.setThresholdValue(updates.getThresholdValue());
        }

        if (updates.getMonitoringFrequency() != null) {
            covenant.setMonitoringFrequency(updates.getMonitoringFrequency());
        }

        return covenantRepository.save(covenant);
    }

    @Transactional
    public EarlyWarningSignal createEWS(Long facilityId, EarlyWarningSignal signal) {
        FacilityAccount facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found"));

        signal.setFacility(facility);
        signal.setDetectedDate(LocalDate.now());
        signal.setStatus(EWSStatus.OPEN);

        return ewsRepository.save(signal);
    }

    @Transactional(readOnly = true)
    public List<EarlyWarningSignal> getEWSByFacility(Long facilityId) {
        return ewsRepository.findByFacility_FacilityId(facilityId);
    }

    @Transactional
    public Notification createNotification(Long userId, Notification notification) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        notification.setUser(user);
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setCreatedDate(LocalDate.now());

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsFiltered(
            Long userId,
            NotificationCategory category,
            NotificationStatus status) {

        return notificationRepository.findByUser_UserId(userId)
                .stream()
                .filter(n -> category == null || n.getCategory() == category)
                .filter(n -> status == null || n.getStatus() == status)
                .toList();
    }

    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setStatus(NotificationStatus.READ);

        return notificationRepository.save(notification);
    }
}
