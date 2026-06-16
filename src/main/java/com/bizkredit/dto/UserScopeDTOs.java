package com.bizkredit.dto;

public class UserScopeDTOs {

    public record UserScopeRequest(
            String branchId,
            String region
    ) {}

    public record UserScopeResponse(
            Long userId,
            String name,
            String email,
            String branchId,
            String region,
            String role
    ) {}
}
