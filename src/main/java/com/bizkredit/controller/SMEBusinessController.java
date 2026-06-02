package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.GroupCompany;
import com.bizkredit.entity.Promoter;
import com.bizkredit.entity.SMEBusiness;
import com.bizkredit.service.SMEBusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class SMEBusinessController {

    private final SMEBusinessService smeBusinessService;

    // POST /api/businesses
    @PostMapping
    public ResponseEntity<ApiResponse<SMEBusiness>> register(@Valid @RequestBody SMEBusiness business) {
        SMEBusiness created = smeBusinessService.registerBusiness(business);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Business registered successfully", created));
    }

    // GET /api/businesses/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SMEBusiness>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Business fetched", smeBusinessService.getBusinessById(id)));
    }

    // GET /api/businesses
    @GetMapping
    public ResponseEntity<ApiResponse<List<SMEBusiness>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("All businesses", smeBusinessService.getAllBusinesses()));
    }

    // PATCH /api/businesses/{id}/kyc?status=Verified
    @PatchMapping("/{id}/kyc")
    public ResponseEntity<ApiResponse<SMEBusiness>> updateKyc(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok("KYC status updated", smeBusinessService.updateKycStatus(id, status)));
    }

    // PATCH /api/businesses/{id}/status?value=Blacklisted
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SMEBusiness>> updateStatus(
            @PathVariable Long id,
            @RequestParam String value) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", smeBusinessService.updateStatus(id, value)));
    }

    // POST /api/businesses/{id}/promoters
    @PostMapping("/{id}/promoters")
    public ResponseEntity<ApiResponse<Promoter>> addPromoter(
            @PathVariable Long id,
            @Valid @RequestBody Promoter promoter) {
        Promoter saved = smeBusinessService.addPromoter(id, promoter);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Promoter added", saved));
    }

    // GET /api/businesses/{id}/promoters
    @GetMapping("/{id}/promoters")
    public ResponseEntity<ApiResponse<List<Promoter>>> getPromoters(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Promoters fetched", smeBusinessService.getPromotersByBusiness(id)));
    }

    // POST /api/businesses/{parentId}/group-links?subsidiaryId=2&relationship=Subsidiary
    @PostMapping("/{parentId}/group-links")
    public ResponseEntity<ApiResponse<GroupCompany>> linkGroup(
            @PathVariable Long parentId,
            @RequestParam Long subsidiaryId,
            @RequestParam String relationship) {
        GroupCompany link = smeBusinessService.linkGroupCompany(parentId, subsidiaryId, relationship);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Group link created", link));
    }

    // GET /api/businesses/{parentId}/group-links
    @GetMapping("/{parentId}/group-links")
    public ResponseEntity<ApiResponse<List<GroupCompany>>> getGroupLinks(@PathVariable Long parentId) {
        return ResponseEntity.ok(ApiResponse.ok("Group links fetched", smeBusinessService.getGroupsByParent(parentId)));
    }
}
