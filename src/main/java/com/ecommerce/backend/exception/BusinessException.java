package com.ecommerce.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown for domain-level rule violations (insufficient stock, duplicate SKU, etc.).
 * Maps to HTTP 422 Unprocessable Entity via {@link GlobalExceptionHandler}.
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
