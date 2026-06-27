package com.bizkredit.module5.controller;

import com.bizkredit.module1.dto.ApiResponse;
import com.bizkredit.module5.entity.Covenant;
import com.bizkredit.module5.entity.EarlyWarningSignal;
import com.bizkredit.module5.entity.Notification;
import com.bizkredit.enums.NotificationCategory;
import com.bizkredit.enums.NotificationStatus;
import com.bizkredit.module5.service.CovenantNotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Module 5: Risk Monitoring & Portfolio")
@RestController
@RequiredArgsConstructor
public class CovenantNotificationController {

    private final CovenantNotificationService covenantService;

    // Covenant

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
    public ResponseEntity<ApiResponse<List<Covenant>>> getCovenants(
            @PathVariable Long facilityId) {

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

    // EWS

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
    public ResponseEntity<ApiResponse<List<EarlyWarningSignal>>> getEWS(
            @PathVariable Long facilityId) {

        return ResponseEntity.ok(ApiResponse.ok("EWS fetched",
                covenantService.getEWSByFacility(facilityId)));
    }

    // Notifications

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

    @PatchMapping("/api/notifications/{id}/read")
    @PreAuthorize("hasAnyRole('SME_APPLICANT','CREDIT_ANALYST','RELATIONSHIP_MANAGER','UNDERWRITING_MANAGER','COLLATERAL_EVALUATOR','ADMIN')")
    public ResponseEntity<ApiResponse<Notification>> markRead(@PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.ok("Notification marked read",
                covenantService.markAsRead(id)));
    }
}