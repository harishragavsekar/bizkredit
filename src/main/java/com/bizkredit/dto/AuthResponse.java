package com.bizkredit.dto;

import com.bizkredit.enums.Role;

// Java 16+ Record - immutable response DTO
// Contains JWT token and user info returned after login/register
public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String name,
        String email,
        Role role
) {}
