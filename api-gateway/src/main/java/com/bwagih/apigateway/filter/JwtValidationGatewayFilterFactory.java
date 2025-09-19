package com.bwagih.apigateway.filter;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final WebClient webClient;

    public JwtValidationGatewayFilterFactory(WebClient.Builder webClientBuilder,
                                             @Value("${auth.service.url}") String authServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // 1. Missing or invalid format token
            if (token == null || !token.startsWith("Bearer ")) {
                return writeErrorResponse(exchange.getResponse(), HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
            }

            // 2. Call auth-service to validate token
            return webClient.get()
                    .uri("/validate")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .exchangeToMono(response -> handleAuthResponse(exchange.getResponse(), response, chain, exchange));
        };
    }


    /**
     * Handle the response from the authentication service.
     */
    private Mono<Void> handleAuthResponse(ServerHttpResponse servletResponse, ClientResponse clientResponse,
                                          GatewayFilterChain chain, ServerWebExchange exchange) {

        log.info("Authentication response: status code {}, uri {}",
                clientResponse.statusCode(), clientResponse.request().getURI());

        HttpStatusCode status = clientResponse.statusCode();

        if (status.is2xxSuccessful()) {
            // Token is valid -> continue the filter chain
            log.info("Token is valid -> continue the filter chain");
            return chain.filter(exchange);
        }

        // Handle specific error codes
        if (status.value() == HttpStatus.UNAUTHORIZED.value()) {
            return writeErrorResponse(servletResponse, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        } else if (status.value() == HttpStatus.FORBIDDEN.value()) {
            return writeErrorResponse(servletResponse, HttpStatus.FORBIDDEN, "Access denied");
        } else if (status.value() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return writeErrorResponse(servletResponse, HttpStatus.INTERNAL_SERVER_ERROR, "Authentication service error");
        }

        // Fallback for other unexpected status codes
        return writeErrorResponse(servletResponse, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error during token validation");
    }

    /**
     * Helper method to write a JSON error response and complete the request.
     */
    private Mono<Void> writeErrorResponse(ServerHttpResponse response, HttpStatus status, String message) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = String.format("{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                status.value(),
                status.getReasonPhrase(),
                message);

        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

}
