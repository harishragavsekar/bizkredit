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

    private final CovenantNotificationService service;

    // ── Covenant endpoints ────────────────────────────────────────

    @PostMapping("/api/covenants")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Covenant>> createCovenant(
            @RequestParam Long facilityId,
            @Valid @RequestBody Covenant covenant) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Covenant created",
                        service.createCovenant(facilityId, covenant)));
    }

    @GetMapping("/api/covenants/{id}")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<Covenant>> getCovenant(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Covenant fetched",
                service.getCovenantById(id)));
    }

    @GetMapping("/api/covenants/facility/{facilityId}")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<Covenant>>> getByFacility(
            @PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok("Covenants fetched",
                service.getCovenantsByFacility(facilityId)));
    }

    @PatchMapping("/api/covenants/{id}/status")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Covenant>> updateStatus(
            @PathVariable Long id,
            @RequestParam CovenantStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Covenant status updated",
                service.updateCovenantStatus(id, status)));
    }

    // ── Covenant Tracking endpoints ───────────────────────────────

    @PostMapping("/api/covenants/{covenantId}/tracking")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<CovenantTracking>> recordTracking(
            @PathVariable Long covenantId,
            @Valid @RequestBody CovenantTracking tracking) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tracking recorded",
                        service.recordTracking(covenantId, tracking)));
    }

    @GetMapping("/api/covenants/{covenantId}/tracking")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<CovenantTracking>>> getTracking(
            @PathVariable Long covenantId) {
        return ResponseEntity.ok(ApiResponse.ok("Tracking fetched",
                service.getTrackingByCovenant(covenantId)));
    }

    // ── Early Warning Signal endpoints ────────────────────────────

    @PostMapping("/api/ews")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<EarlyWarningSignal>> createEWS(
            @RequestParam Long facilityId,
            @Valid @RequestBody EarlyWarningSignal signal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("EWS created",
                        service.createEWS(facilityId, signal)));
    }

    @GetMapping("/api/ews/facility/{facilityId}")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<EarlyWarningSignal>>> getEWSByFacility(
            @PathVariable Long facilityId) {
        return ResponseEntity.ok(ApiResponse.ok("EWS fetched",
                service.getEWSByFacility(facilityId)));
    }

    @PatchMapping("/api/ews/{id}/status")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<EarlyWarningSignal>> updateEWSStatus(
            @PathVariable Long id,
            @RequestParam EWSStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("EWS status updated",
                service.updateEWSStatus(id, status)));
    }

    @GetMapping("/api/ews/status")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','CREDIT_ANALYST','ADMIN')")
    public ResponseEntity<ApiResponse<List<EarlyWarningSignal>>> getEWSByStatus(
            @RequestParam EWSStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("EWS fetched",
                service.getEWSByStatus(status)));
    }

    // ── Notification endpoints ────────────────────────────────────

    @PostMapping("/api/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Notification>> createNotification(
            @RequestParam Long userId,
            @Valid @RequestBody Notification notification) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Notification created",
                        service.createNotification(userId, notification)));
    }

    @GetMapping("/api/notifications/user/{userId}")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','UNDERWRITING_MANAGER','COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Notifications fetched",
                service.getNotificationsByUser(userId)));
    }

    @GetMapping("/api/notifications/user/{userId}/unread")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','UNDERWRITING_MANAGER','COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnread(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Unread notifications fetched",
                service.getUnreadNotifications(userId)));
    }

    @PatchMapping("/api/notifications/{id}/read")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','UNDERWRITING_MANAGER','COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read",
                service.markAsRead(id)));
    }

    @PatchMapping("/api/notifications/{id}/dismiss")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','UNDERWRITING_MANAGER','COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<Notification>> dismiss(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Notification dismissed",
                service.dismissNotification(id)));
    }
}
