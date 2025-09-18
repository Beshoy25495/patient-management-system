package com.bwagih.patientservice.exception;

import com.bwagih.patientservice.dto.ApiGlobalErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class CustomExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(CustomExceptionHandler.class);

    /**
     * Handle email already exists exceptions globally.
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiGlobalErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Email address already exist {}", ex.getMessage());

        String userMessage = ex.getMessage();

        ApiGlobalErrorResponse errorResponse = ApiGlobalErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Email address already exists")
                .message(userMessage)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);

    }

    /**
     * Handle entity not found exceptions globally.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiGlobalErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
        log.warn("Entity not found {}", ex.getMessage());

        String userMessage = ex.getMessage();

        ApiGlobalErrorResponse errorResponse = ApiGlobalErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Entity not found")
                .message(userMessage)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

}
