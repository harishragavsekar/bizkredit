package com.bizkredit.service;

import com.bizkredit.entity.*;
import com.bizkredit.enums.*;
import com.bizkredit.exception.BadRequestException;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CovenantNotificationService {

    private final CovenantRepository covenantRepository;
    private final CovenantTrackingRepository trackingRepository;
    private final EarlyWarningSignalRepository ewsRepository;
    private final NotificationRepository notificationRepository;
    private final FacilityAccountRepository facilityRepository;
    private final UserRepository userRepository;

    // ── 4.7 Covenant ─────────────────────────────────────────────

    @Transactional
    public Covenant createCovenant(Long facilityId, Covenant covenant) {
        FacilityAccount facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found: " + facilityId));
        covenant.setFacility(facility);
        covenant.setStatus(CovenantStatus.ACTIVE);
        Covenant saved = covenantRepository.save(covenant);
        log.info("Covenant created for facility {}: {}", facilityId, saved.getDescription());
        return saved;
    }

    @Transactional(readOnly = true)
    public Covenant getCovenantById(Long covenantId) {
        return covenantRepository.findById(covenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Covenant not found: " + covenantId));
    }

    @Transactional(readOnly = true)
    public List<Covenant> getCovenantsByFacility(Long facilityId) {
        return covenantRepository.findByFacility_FacilityId(facilityId);
    }

    @Transactional
    public Covenant updateCovenantStatus(Long covenantId, CovenantStatus status) {
        Covenant covenant = getCovenantById(covenantId);
        covenant.setStatus(status);
        log.info("Covenant {} status updated to {}", covenantId, status);
        return covenantRepository.save(covenant);
    }

    @Transactional
    public CovenantTracking recordTracking(Long covenantId, CovenantTracking tracking) {
        Covenant covenant = getCovenantById(covenantId);
        tracking.setCovenant(covenant);
        tracking.setReviewDate(LocalDate.now());

        if (tracking.getActualValue() != null && tracking.getThresholdValue() != null) {
            boolean breached = tracking.getActualValue()
                    .compareTo(tracking.getThresholdValue()) < 0;
            tracking.setComplianceStatus(breached
                    ? ComplianceStatus.BREACHED
                    : ComplianceStatus.COMPLIANT);

            if (breached) {
                covenant.setStatus(CovenantStatus.BREACHED);
                covenantRepository.save(covenant);
                log.warn("Covenant {} breached for period {}", covenantId, tracking.getPeriod());
            }
        }

        return trackingRepository.save(tracking);
    }

    @Transactional(readOnly = true)
    public List<CovenantTracking> getTrackingByCovenant(Long covenantId) {
        return trackingRepository.findByCovenant_CovenantId(covenantId);
    }

    @Transactional
    public EarlyWarningSignal createEWS(Long facilityId, EarlyWarningSignal signal) {
        FacilityAccount facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found: " + facilityId));
        signal.setFacility(facility);
        signal.setDetectedDate(LocalDate.now());
        signal.setStatus(EWSStatus.OPEN);
        EarlyWarningSignal saved = ewsRepository.save(signal);
        log.warn("EWS created for facility {}: {} - {}", facilityId, signal.getSignalType(), signal.getSeverity());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<EarlyWarningSignal> getEWSByFacility(Long facilityId) {
        return ewsRepository.findByFacility_FacilityId(facilityId);
    }

    @Transactional
    public EarlyWarningSignal updateEWSStatus(Long ewsId, EWSStatus status) {
        EarlyWarningSignal signal = ewsRepository.findById(ewsId)
                .orElseThrow(() -> new ResourceNotFoundException("EWS not found: " + ewsId));
        signal.setStatus(status);
        log.info("EWS {} status updated to {}", ewsId, status);
        return ewsRepository.save(signal);
    }

    @Transactional(readOnly = true)
    public List<EarlyWarningSignal> getEWSByStatus(EWSStatus status) {
        return ewsRepository.findByStatus(status);
    }

    // ── 4.8 Notifications ─────────────────────────────────────────

    @Transactional
    public Notification createNotification(Long userId, Notification notification) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        notification.setUser(user);
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setCreatedDate(LocalDate.now());
        Notification saved = notificationRepository.save(notification);
        log.info("Notification created for user {}: {}", userId, saved.getCategory());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUser(Long userId) {
        return notificationRepository.findByUser_UserId(userId);
    }

    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        notification.setStatus(NotificationStatus.READ);
        log.info("Notification {} marked as READ", notificationId);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification dismissNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));

        if (notification.getStatus() == NotificationStatus.DISMISSED) {
            throw new BadRequestException("Notification already dismissed");
        }

        notification.setStatus(NotificationStatus.DISMISSED);
        log.info("Notification {} dismissed", notificationId);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUser_UserId(userId)
                .stream()
                .filter(n -> n.getStatus() == NotificationStatus.UNREAD)
                .toList();
    }
}
