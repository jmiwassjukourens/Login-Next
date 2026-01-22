package com.app.springbootcrud.controllers;

import java.util.Arrays;
import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.app.springbootcrud.controllers.dto.ErrorResponse;

@Component
public class ErrorHandler {

    private final Environment environment;

    public ErrorHandler(Environment environment) {
        this.environment = environment;
    }

    public boolean isProduction() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    public ResponseEntity<ErrorResponse> createErrorResponse(
            String genericMessage,
            String detailedMessage,
            HttpStatus status
    ) {
        String message = isProduction() ? genericMessage : detailedMessage;
        return ResponseEntity.status(status).body(new ErrorResponse(message));
    }

    public ResponseEntity<ErrorResponse> createValidationErrorResponse(
            Map<String, String> errors
    ) {
        // In production, don't expose field-level validation details
        if (isProduction()) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Validation failed. Please check your input."));
        }
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("Validation failed", errors));
    }

    public ResponseEntity<ErrorResponse> createNotFoundResponse(String resource) {
        String message = isProduction() 
            ? "Resource not found" 
            : resource + " not found";
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(message));
    }
}
