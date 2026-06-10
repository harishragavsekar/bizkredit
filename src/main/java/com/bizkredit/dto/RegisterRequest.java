package com.bizkredit.dto;

import com.bizkredit.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Java 16+ Record - immutable DTO for user registration
// Accessors: name(), email(), password(), phone(), role(), branchId()
public record RegisterRequest(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String phone,
        @NotNull Role role,
        String branchId
) {}
