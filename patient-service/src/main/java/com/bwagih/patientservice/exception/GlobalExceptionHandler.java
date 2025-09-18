package com.bwagih.patientservice.exception;

import com.bwagih.patientservice.dto.ApiGlobalErrorResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiGlobalErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Collect first validation error (or you could combine them into a single string)
        String firstErrorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation error");

        ApiGlobalErrorResponse errorResponse = ApiGlobalErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message(firstErrorMessage)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle database constraint violations (e.g., unique constraint, foreign key violations).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiGlobalErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Database constraint violation: {}", ex.getMessage());

        String userMessage = getUserMessage(ex);

        ApiGlobalErrorResponse errorResponse = ApiGlobalErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Data Integrity Violation")
                .message(userMessage)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    private static String getUserMessage(DataIntegrityViolationException ex) {
        String rootMessage = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        String fieldName = extractDuplicateFieldName(rootMessage);
        String userMessage = "A database constraint was violated.";

        // Try to provide a descriptive message
        if (rootMessage != null) {
            if (rootMessage.toLowerCase().contains("unique")) {

                if (fieldName != null) {
                    userMessage = "Duplicate value violation on field: " + fieldName.toLowerCase();
                } else {
                    userMessage = "Duplicate value violation. A record with this value already exists.";
                }

            } else if (rootMessage.toLowerCase().contains("foreign key")) {
                userMessage = "This record cannot be saved because it is linked to other data.";
            }
        }
        return userMessage;
    }


    private static String extractDuplicateFieldName(String rootMessage) {
        if (rootMessage == null || rootMessage.isEmpty()) {
            return null;
        }

        String lowerMsg = rootMessage.toLowerCase();

        // 1. H2 (SQL in-memory)
        if (lowerMsg.contains("unique index") || lowerMsg.contains("primary key violation")) {
            // Example: Unique index or primary key violation:
            // "PUBLIC.CONSTRAINT_INDEX_F ON PUBLIC.PATIENT(EMAIL NULLS FIRST)"
            int start = rootMessage.indexOf('(');
            int end = rootMessage.indexOf(')', start);
            if (start != -1 && end != -1) {
                return rootMessage.substring(start + 1, end)
                        .replace("NULLS FIRST", "")
                        .trim();
            }
        }

        // 2. PostgreSQL
        // Example: Key (email)=(test@example.com) already exists.
        if (lowerMsg.contains("duplicate key value violates unique constraint")) {
            int start = lowerMsg.indexOf("key (");
            int end = lowerMsg.indexOf(")=", start);
            if (start != -1 && end != -1) {
                return rootMessage.substring(start + 5, end).trim();
            }
        }

        // 3. Oracle
        // Example: ORA-00001: unique constraint (PATIENT_EMAIL_UK) violated
        if (lowerMsg.contains("ora-00001")) {
            // Oracle does not directly specify column, so return the constraint name
            int start = rootMessage.indexOf('(');
            int end = rootMessage.indexOf(')', start);
            if (start != -1 && end != -1) {
                return "Constraint: " + rootMessage.substring(start + 1, end).trim();
            }
        }

        // 4. MongoDB
        // Example: E11000 duplicate key error collection: patientdb.patients index: email_1 dup key: { email: "test@example.com" }
        if (lowerMsg.contains("e11000 duplicate key error")) {
            int index = lowerMsg.indexOf("index:");
            if (index != -1) {
                String substring = rootMessage.substring(index + 6).trim();
                int spaceIndex = substring.indexOf(' ');
                if (spaceIndex != -1) {
                    String indexName = substring.substring(0, spaceIndex).trim();
                    return indexName.replace("_1", ""); // MongoDB uses _1 or _-1 for sort order
                }
            }
        }

        // 5. Elasticsearch
        // Example: version_conflict_engine_exception: [patient][1]: version conflict, document already exists
        if (lowerMsg.contains("version_conflict_engine_exception")) {
            return "document id"; // Elasticsearch conflict usually due to same document ID
        }

        return null; // fallback if we can't determine field
    }


    /**
     * Handle ALL parsing-related errors globally.
     * This includes:
     * - JSON deserialization errors
     * - Invalid date formats
     * - Invalid number formats
     * - Malformed JSON
     */
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            DateTimeParseException.class,
            NumberFormatException.class
    })
    public ResponseEntity<ApiGlobalErrorResponse> handleGlobalParsingExceptions(Exception ex, HttpServletRequest request) {
        log.error("Global parsing error: {}", ex.getMessage(), ex);

        // Extract root cause to better determine what happened
        Throwable rootCause = getRootCause(ex);

        // Default fallback message
        String message = "Invalid input format provided.";

        // 1. Extract field and class info if possible
        Map<String, String> fieldInfo = extractFieldAndClassFromJacksonMessage(ex.getMessage());
        String fieldName = fieldInfo.getOrDefault("fieldName", "unknown");
        String className = fieldInfo.getOrDefault("className", null);

        // 2. Determine error type and customize message
        if (rootCause instanceof DateTimeParseException) {
            // Handle invalid date format
            String expectedFormat = getExpectedFormatFromEntity(className, fieldName);

            if (!"unknown".equalsIgnoreCase(fieldName)) {
                message = "Invalid date format for field '" + fieldName +
                        "'. Expected format: " + expectedFormat;
            } else {
                message = "Invalid date format. Expected format: " + expectedFormat;
            }

        } else if (rootCause instanceof NumberFormatException) {
            // Handle invalid number format
            message = fieldName != null && !fieldName.equals("unknown")
                    ? "Invalid number format for field '" + fieldName + "'. Please provide a valid numeric value."
                    : "Invalid number format. Please ensure numeric fields contain valid numbers.";

        } else if (rootCause instanceof HttpMessageNotReadableException) {
            // Generic Jackson parsing issue
            message = "Malformed JSON request or invalid data type.";
        } else if (rootCause instanceof IllegalArgumentException) {
            // Handle invalid arguments
            message = "Invalid arguments provided.";
        } else {
            // Handle other parsing errors
            message = "Invalid input format provided.";
        }

        // 3. Build the error response
        ApiGlobalErrorResponse errorResponse = ApiGlobalErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Parsing Error")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        return (cause == null || cause == throwable) ? throwable : getRootCause(cause);
    }


    private Map<String, String> extractFieldAndClassFromJacksonMessage(String jacksonMessage) {
        Map<String, String> result = new HashMap<>();
        if (jacksonMessage == null) return result;

        // Find reference chain part
        int chainIndex = jacksonMessage.lastIndexOf("(through reference chain:");
        if (chainIndex != -1) {
            String sub = jacksonMessage.substring(chainIndex);
            // Example: (through reference chain: com.example.PatientDto["birthDate"])
            int classStart = sub.indexOf(':') + 1;
            int fieldStart = sub.indexOf("[\"");
            int fieldEnd = sub.indexOf("\"]");

            if (classStart > 0 && fieldStart > 0 && fieldEnd > fieldStart) {
                String className = sub.substring(classStart, fieldStart).trim();
                String fieldName = sub.substring(fieldStart + 2, fieldEnd);

                result.put("className", className);
                result.put("fieldName", fieldName);
            }
        }
        return result;
    }


    private String getExpectedFormatFromEntity(String className, String fieldName) {

        if (className != null) {

            try {

                Class<?> clazz = Class.forName(className); // Dynamically load class
                var field = clazz.getDeclaredField(fieldName);

                // 1. Check for @JsonFormat
                JsonFormat jsonFormat = field.getAnnotation(JsonFormat.class);
                if (jsonFormat != null && !jsonFormat.pattern().isEmpty()) {
                    return jsonFormat.pattern();
                }

                // 2. Check for @DateTimeFormat
                DateTimeFormat dateTimeFormat = field.getAnnotation(DateTimeFormat.class);
                if (dateTimeFormat != null && !dateTimeFormat.pattern().isEmpty()) {
                    return dateTimeFormat.pattern();
                }

                // 3. If no annotation, detect based on type
                Class<?> fieldType = field.getType();

                if (fieldType.equals(LocalDate.class)) {
                    return "yyyy-MM-dd";
                } else if (fieldType.equals(LocalDateTime.class)) {
                    return "yyyy-MM-dd'T'HH:mm:ss";
                } else if (fieldType.equals(java.util.Date.class) || fieldType.equals(java.sql.Date.class)) {
                    return "yyyy-MM-dd HH:mm:ss";
                } else if (fieldType.equals(OffsetDateTime.class)) {
                    return "yyyy-MM-dd'T'HH:mm:ssXXX"; // ISO-8601 with timezone
                } else if (fieldType.equals(ZonedDateTime.class)) {
                    return "yyyy-MM-dd'T'HH:mm:ss.SSSZ"; // With timezone offset
                }

            } catch (ClassNotFoundException | NoSuchFieldException ignored) {
                // Could not find class or field, return fallback format
                log.error("Could not find class or field: {}", ignored.getMessage(), ignored);
                return "yyyy-MM-dd";
            }
        }

        // 4. Global fallback format
        return "yyyy-MM-dd";
    }


    /**
     * Catch-all for any other unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiGlobalErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception occurred: ", ex);

        ApiGlobalErrorResponse errorResponse = ApiGlobalErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getRequestURI())
                .build();


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
