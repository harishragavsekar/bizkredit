package com.bizkredit.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Java 16+ Record - immutable DTO, replaces @Data class
// Accessors: email(), password() - no getters needed
public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {}
