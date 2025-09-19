package com.bwagih.authservice.utils;

import com.bwagih.authservice.dto.APIBusinessLogicResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class APIResponseHandler {

    public static <RES extends APIBusinessLogicResponse<RESULT>, RESULT> ResponseEntity<RES> createResponse(HttpStatus statusCode, RES body) {
        return ResponseEntity
                .status(statusCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}
