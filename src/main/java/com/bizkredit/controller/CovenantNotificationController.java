package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.*;
import com.bizkredit.enums.*;
import com.bizkredit.service.CovenantNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CovenantNotificationController {

    private final CovenantNotificationService covenantService;

    // ── Covenant ──────────────────────────────────────────────────

    @PostMapping("/api/facilities/{facilityId}/covenants")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Covenant>> createCovenant(
            @PathVariable Long facilityId,
            @Valid @RequestBody Covenant covenant) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Covenant created",
                        covenantService.createCovenant(facilityId, covenant)));
    }

    @GetMapping("/api/facilities/{facilityId}/covenants")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<Covenant>>> getCovenants(@PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok("Covenants fetched",
                covenantService.getCovenantsByFacility(facilityId)));
    }

    @PutMapping("/api/facilities/{facilityId}/covenants/{id}")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Covenant>> updateCovenant(
            @PathVariable Long facilityId,
            @PathVariable Long id,
            @RequestBody Covenant updates) {
        return ResponseEntity.ok(ApiResponse.ok("Covenant updated",
                covenantService.updateCovenant(id, updates)));
    }

    @PatchMapping("/api/facilities/{facilityId}/covenants/{id}/status")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Covenant>> updateCovenantStatus(
            @PathVariable Long facilityId,
            @PathVariable Long id,
            @RequestParam CovenantStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                covenantService.updateCovenantStatus(id, status)));
    }

    @PatchMapping("/api/facilities/{facilityId}/covenants/{id}/waive")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Covenant>> waiveCovenant(
            @PathVariable Long facilityId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Covenant waived",
                covenantService.waiveCovenant(id)));
    }

    @PostMapping("/api/covenants/{covenantId}/tracking")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<CovenantTracking>> recordTracking(
            @PathVariable Long covenantId,
            @Valid @RequestBody CovenantTracking tracking) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tracking recorded",
                        covenantService.recordTracking(covenantId, tracking)));
    }

    @GetMapping("/api/covenants/{covenantId}/tracking")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<CovenantTracking>>> getTracking(
            @PathVariable Long covenantId) {
        return ResponseEntity.ok(ApiResponse.ok("Tracking fetched",
                covenantService.getTrackingByCovenant(covenantId)));
    }

    // ── EWS ──────────────────────────────────────────────────────

    @PostMapping("/api/facilities/{facilityId}/ews")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<EarlyWarningSignal>> createEWS(
            @PathVariable Long facilityId,
            @Valid @RequestBody EarlyWarningSignal signal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("EWS created",
                        covenantService.createEWS(facilityId, signal)));
    }

    @GetMapping("/api/facilities/{facilityId}/ews")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<EarlyWarningSignal>>> getEWSByFacility(
            @PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok("EWS fetched",
                covenantService.getEWSByFacility(facilityId)));
    }

    @GetMapping("/api/ews")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<EarlyWarningSignal>>> getEWSFiltered(
            @RequestParam(required = false) EWSSeverity severity,
            @RequestParam(required = false) EWSStatus status,
            @RequestParam(required = false) EWSSignalType signalType) {
        return ResponseEntity.ok(ApiResponse.ok("EWS fetched",
                covenantService.getEWSFiltered(severity, status, signalType)));
    }

    @PatchMapping("/api/ews/{ewsId}/action")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<EarlyWarningSignal>> actionEWS(@PathVariable Long ewsId) {
        return ResponseEntity.ok(ApiResponse.ok("EWS actioned", covenantService.actionEWS(ewsId)));
    }

    @PatchMapping("/api/ews/{ewsId}/clear")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<EarlyWarningSignal>> clearEWS(@PathVariable Long ewsId) {
        return ResponseEntity.ok(ApiResponse.ok("EWS cleared", covenantService.clearEWS(ewsId)));
    }

    @PatchMapping("/api/ews/{id}/status")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<EarlyWarningSignal>> updateEWSStatus(
            @PathVariable Long id, @RequestParam EWSStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("EWS status updated",
                covenantService.updateEWSStatus(id, status)));
    }

    @GetMapping("/api/ews/status")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<EarlyWarningSignal>>> getByStatus(
            @RequestParam EWSStatus value) {
        return ResponseEntity.ok(ApiResponse.ok("EWS fetched", covenantService.getEWSByStatus(value)));
    }

    // ── Notifications ─────────────────────────────────────────────

    @PostMapping("/api/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Notification>> createNotification(
            @RequestParam Long userId,
            @Valid @RequestBody Notification notification) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Notification created",
                        covenantService.createNotification(userId, notification)));
    }

    @GetMapping("/api/notifications")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','UNDERWRITING_MANAGER','COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(
            @RequestParam Long userId,
            @RequestParam(required = false) NotificationCategory category,
            @RequestParam(required = false) NotificationStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Notifications fetched",
                covenantService.getNotificationsFiltered(userId, category, status)));
    }

    @GetMapping("/api/notifications/user/{userId}")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','UNDERWRITING_MANAGER','COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<List<Notification>>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Notifications fetched",
                covenantService.getNotificationsByUser(userId)));
    }

    @GetMapping("/api/notifications/unread-count")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','UNDERWRITING_MANAGER','COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Unread count",
                covenantService.getUnreadCount(userId)));
    }

    @GetMapping("/api/notifications/user/{userId}/unread")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','UNDERWRITING_MANAGER','COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnread(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Unread notifications",
                covenantService.getUnreadNotifications(userId)));
    }

    @PatchMapping("/api/notifications/{id}/read")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','UNDERWRITING_MANAGER','COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<Notification>> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Notification marked read",
                covenantService.markAsRead(id)));
    }

    @PatchMapping("/api/notifications/{id}/dismiss")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','UNDERWRITING_MANAGER','COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<Notification>> dismiss(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Notification dismissed",
                covenantService.dismissNotification(id)));
    }
}
