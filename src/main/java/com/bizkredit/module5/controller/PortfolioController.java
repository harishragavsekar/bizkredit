package com.bizkredit.module5.controller;

import com.bizkredit.module1.dto.ApiResponse;
import com.bizkredit.module5.service.PortfolioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Module 5: Risk Monitoring & Portfolio")
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok("Portfolio summary",
                portfolioService.getPortfolioSummary()));
    }

    @GetMapping("/asset-quality")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAssetQuality() {
        return ResponseEntity.ok(ApiResponse.ok("Asset quality distribution",
                portfolioService.getAssetQuality()));
    }

    @GetMapping("/sector-exposure")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSectorExposure() {
        return ResponseEntity.ok(ApiResponse.ok("Sector-wise exposure",
                portfolioService.getSectorExposure()));
    }
}
