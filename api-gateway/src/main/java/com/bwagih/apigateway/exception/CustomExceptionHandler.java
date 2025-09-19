package com.bwagih.apigateway.exception;

import com.bwagih.apigateway.dto.ApiGlobalErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class CustomExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(CustomExceptionHandler.class);

    /**
     * Handle unauthorized exceptions globally
     */
    @ExceptionHandler(WebClientResponseException.Unauthorized.class)
    public Mono<ResponseEntity<ApiGlobalErrorResponse>> handleUnauthorizedException(ServerWebExchange exchange){
        log.error("Unauthorized exception occurred for path: {}", exchange.getRequest().getURI());

        ApiGlobalErrorResponse errorResponse = ApiGlobalErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("UNAUTHORIZED")
                .message("Invalid or expired token")
                .path(exchange.getRequest().getURI().toString())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
    }

    /**
     * Handle forbidden exceptions globally
     */
    @ExceptionHandler(WebClientResponseException.Forbidden.class)
    public Mono<ResponseEntity<ApiGlobalErrorResponse>> handleForbiddenException(ServerWebExchange exchange){
        log.error("Forbidden exception occurred for path: {}", exchange.getRequest().getURI());

        ApiGlobalErrorResponse errorResponse = ApiGlobalErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error("FORBIDDEN")
                .message("Access denied")
                .path(exchange.getRequest().getURI().toString())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse));
    }

}
