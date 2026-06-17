package com.bizkredit.dto;

//Response body for POST /api/auth/forgot-password.

public record ForgotPasswordResponse(String resetToken) {
}
