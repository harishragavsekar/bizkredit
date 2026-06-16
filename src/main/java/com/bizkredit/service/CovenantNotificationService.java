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
    private final AuditLogService auditLogService;
    private final NotificationHelper notificationHelper;

    // ── 4.7 Covenant ─────────────────────────────────────────────

    @Transactional
    public Covenant createCovenant(Long facilityId, Covenant covenant) {
        FacilityAccount facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found: " + facilityId));
        covenant.setFacility(facility);
        covenant.setStatus(CovenantStatus.ACTIVE);
        Covenant saved = covenantRepository.save(covenant);
        auditLogService.log(null, "CREATE", "Covenant", String.valueOf(saved.getCovenantId()));
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

    // PUT /api/facilities/{facilityId}/covenants/{id} — update description/threshold
    @Transactional
    public Covenant updateCovenant(Long covenantId, Covenant updates) {
        Covenant existing = getCovenantById(covenantId);
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getThresholdValue() != null) existing.setThresholdValue(updates.getThresholdValue());
        if (updates.getMonitoringFrequency() != null) existing.setMonitoringFrequency(updates.getMonitoringFrequency());
        auditLogService.log(null, "UPDATE", "Covenant", String.valueOf(covenantId));
        return covenantRepository.save(existing);
    }

    @Transactional
    public Covenant updateCovenantStatus(Long covenantId, CovenantStatus status) {
        Covenant covenant = getCovenantById(covenantId);
        covenant.setStatus(status);
        auditLogService.log(null, "STATUS_CHANGE", "Covenant", String.valueOf(covenantId));
        log.info("Covenant {} status updated to {}", covenantId, status);
        return covenantRepository.save(covenant);
    }

    // PATCH /api/facilities/{facilityId}/covenants/{id}/waive
    @Transactional
    public Covenant waiveCovenant(Long covenantId) {
        Covenant covenant = getCovenantById(covenantId);
        covenant.setStatus(CovenantStatus.WAIVED);
        auditLogService.log(null, "STATUS_CHANGE", "Covenant", String.valueOf(covenantId));
        log.info("Covenant {} waived", covenantId);
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
            tracking.setComplianceStatus(breached ? ComplianceStatus.BREACHED : ComplianceStatus.COMPLIANT);

            if (breached) {
                covenant.setStatus(CovenantStatus.BREACHED);
                covenantRepository.save(covenant);
                log.warn("Covenant {} breached for period {}", covenantId, tracking.getPeriod());

                // Auto-generate EWS signal on breach
                Long facilityId = covenant.getFacility().getFacilityId();
                createEWS(facilityId, EarlyWarningSignal.builder()
                        .signalType(EWSSignalType.COVENANT_BREACH)
                        .severity(EWSSeverity.RED)
                        .build());
            }
        }

        CovenantTracking saved = trackingRepository.save(tracking);
        auditLogService.log(null, "CREATE", "CovenantTracking", String.valueOf(saved.getTrackingId()));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CovenantTracking> getTrackingByCovenant(Long covenantId) {
        return trackingRepository.findByCovenant_CovenantId(covenantId);
    }

    // ── EWS ──────────────────────────────────────────────────────

    @Transactional
    public EarlyWarningSignal createEWS(Long facilityId, EarlyWarningSignal signal) {
        FacilityAccount facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found: " + facilityId));
        signal.setFacility(facility);
        signal.setDetectedDate(LocalDate.now());
        signal.setStatus(EWSStatus.OPEN);
        EarlyWarningSignal saved = ewsRepository.save(signal);
        auditLogService.log(null, "CREATE", "EarlyWarningSignal", String.valueOf(saved.getEwsId()));
        log.warn("EWS created for facility {}: {} - {}", facilityId, signal.getSignalType(), signal.getSeverity());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<EarlyWarningSignal> getEWSByFacility(Long facilityId) {
        return ewsRepository.findByFacility_FacilityId(facilityId);
    }

    @Transactional
    public EarlyWarningSignal actionEWS(Long ewsId) {
        EarlyWarningSignal signal = ewsRepository.findById(ewsId)
                .orElseThrow(() -> new ResourceNotFoundException("EWS not found: " + ewsId));
        if (signal.getStatus() != EWSStatus.OPEN) {
            throw new BadRequestException("Only OPEN EWS signals can be actioned");
        }
        signal.setStatus(EWSStatus.ACTIONED);
        auditLogService.log(null, "STATUS_CHANGE", "EarlyWarningSignal", String.valueOf(ewsId));
        return ewsRepository.save(signal);
    }

    @Transactional
    public EarlyWarningSignal clearEWS(Long ewsId) {
        EarlyWarningSignal signal = ewsRepository.findById(ewsId)
                .orElseThrow(() -> new ResourceNotFoundException("EWS not found: " + ewsId));
        if (signal.getStatus() != EWSStatus.ACTIONED) {
            throw new BadRequestException("Only ACTIONED EWS signals can be cleared");
        }
        signal.setStatus(EWSStatus.CLEARED);
        auditLogService.log(null, "STATUS_CHANGE", "EarlyWarningSignal", String.valueOf(ewsId));
        return ewsRepository.save(signal);
    }

    @Transactional
    public EarlyWarningSignal updateEWSStatus(Long ewsId, EWSStatus status) {
        EarlyWarningSignal signal = ewsRepository.findById(ewsId)
                .orElseThrow(() -> new ResourceNotFoundException("EWS not found: " + ewsId));
        signal.setStatus(status);
        auditLogService.log(null, "STATUS_CHANGE", "EarlyWarningSignal", String.valueOf(ewsId));
        log.info("EWS {} status updated to {}", ewsId, status);
        return ewsRepository.save(signal);
    }

    @Transactional(readOnly = true)
    public List<EarlyWarningSignal> getEWSByStatus(EWSStatus status) {
        return ewsRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<EarlyWarningSignal> getEWSFiltered(EWSSeverity severity, EWSStatus status, EWSSignalType signalType) {
        if (severity != null) return ewsRepository.findBySeverity(severity);
        if (status != null) return ewsRepository.findByStatus(status);
        return ewsRepository.findAll();
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

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsFiltered(Long userId, NotificationCategory category, NotificationStatus status) {
        List<Notification> all = notificationRepository.findByUser_UserId(userId);
        return all.stream()
                .filter(n -> category == null || n.getCategory() == category)
                .filter(n -> status == null || n.getStatus() == status)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.findByUser_UserId(userId).stream()
                .filter(n -> n.getStatus() == NotificationStatus.UNREAD)
                .count();
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
        return notificationRepository.findByUser_UserId(userId).stream()
                .filter(n -> n.getStatus() == NotificationStatus.UNREAD)
                .toList();
    }
}
