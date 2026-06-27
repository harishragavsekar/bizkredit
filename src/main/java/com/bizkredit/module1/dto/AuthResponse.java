package com.bizkredit.module1.dto;

import com.bizkredit.enums.Role;


public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String name,
        String email,
        Role role
) {}
