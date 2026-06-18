package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.LoanProduct;
import com.bizkredit.enums.LoanProductStatus;
import com.bizkredit.service.LoanProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Module 2: SME Onboarding & Loan Origination", description = "Loan product catalog")
@RestController
@RequestMapping("/api/loan-products")
@RequiredArgsConstructor
public class LoanProductController {

    private final LoanProductService loanProductService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanProduct>> create(
            @Valid @RequestBody LoanProduct product,
            @RequestParam Long createdById) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Loan product created",
                        loanProductService.createProduct(product, createdById)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_ANALYST','RELATIONSHIP_MANAGER')")
    public ResponseEntity<ApiResponse<List<LoanProduct>>> getAll(
            @RequestParam(required = false) LoanProductStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Loan products fetched",
                loanProductService.getProducts(status)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_ANALYST','RELATIONSHIP_MANAGER')")
    public ResponseEntity<ApiResponse<LoanProduct>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Loan product fetched",
                loanProductService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanProduct>> update(
            @PathVariable Long id, @RequestBody LoanProduct updates) {
        return ResponseEntity.ok(ApiResponse.ok("Loan product updated",
                loanProductService.updateProduct(id, updates)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanProduct>> updateStatus(
            @PathVariable Long id, @RequestParam LoanProductStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                loanProductService.updateStatus(id, status)));
    }
}
