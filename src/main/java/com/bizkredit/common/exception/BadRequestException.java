package com.bizkredit.common.exception;

// Thrown when request data violates a business rule (e.g. duplicate email)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
