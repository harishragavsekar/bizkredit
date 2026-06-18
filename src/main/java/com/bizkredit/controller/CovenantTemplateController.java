package com.bizkredit.controller;

import com.bizkredit.dto.ApiResponse;
import com.bizkredit.entity.Covenant;
import com.bizkredit.entity.CovenantTemplate;
import com.bizkredit.enums.CovenantType;
import com.bizkredit.service.CovenantTemplateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Module 5: Risk Monitoring & Portfolio", description = "Covenant templates")
@RestController
@RequestMapping("/api/covenant-templates")
@RequiredArgsConstructor
public class CovenantTemplateController {

    private final CovenantTemplateService templateService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CovenantTemplate>> create(
            @Valid @RequestBody CovenantTemplate template,
            @RequestParam Long createdById) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Template created",
                        templateService.createTemplate(template, createdById)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RELATIONSHIP_MANAGER')")
    public ResponseEntity<ApiResponse<List<CovenantTemplate>>> getAll(
            @RequestParam(required = false) CovenantType covenantType) {
        return ResponseEntity.ok(ApiResponse.ok("Templates fetched",
                templateService.getTemplates(covenantType)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RELATIONSHIP_MANAGER')")
    public ResponseEntity<ApiResponse<CovenantTemplate>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Template fetched", templateService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CovenantTemplate>> update(
            @PathVariable Long id, @RequestBody CovenantTemplate updates) {
        return ResponseEntity.ok(ApiResponse.ok("Template updated",
                templateService.updateTemplate(id, updates)));
    }

    // POST /api/covenant-templates/{id}/apply?facilityId={id}
    @PostMapping("/{id}/apply")
    @PreAuthorize("hasAnyRole('RELATIONSHIP_MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Covenant>> apply(
            @PathVariable Long id, @RequestParam Long facilityId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Template applied to facility",
                        templateService.applyTemplate(id, facilityId)));
    }
}
