package com.bwagih.apigateway.exception;

import com.bwagih.apigateway.dto.ApiGlobalErrorResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends CustomExceptionHandler{

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /**
     * Handle validation errors (e.g., @Valid annotations).
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ApiGlobalErrorResponse> handleUnsupportedOperationException(UnsupportedOperationException ex, ServerWebExchange request) {
        log.error("Unsupported operation exception occurred: ", ex);
        String errorMessage = ex.getMessage();
        ApiGlobalErrorResponse errorResponse = ApiGlobalErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Unsupported Operation Exception")
                .message(errorMessage)
                .path(request.getRequest().getURI().toString())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }


    /**
     * Catch-all for any other unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiGlobalErrorResponse> handleGenericException(
            Exception ex,
            ServerWebExchange request
    ) {
        log.error("Unhandled exception occurred: ", ex);

        ApiGlobalErrorResponse errorResponse = ApiGlobalErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getRequest().getURI().toString())
                .build();


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
