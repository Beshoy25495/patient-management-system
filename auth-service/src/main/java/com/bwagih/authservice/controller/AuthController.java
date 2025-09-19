package com.bwagih.authservice.controller;

import com.bwagih.authservice.dto.APIBusinessLogicResponse;
import com.bwagih.authservice.dto.LoginRequestDTO;
import com.bwagih.authservice.dto.LoginResponseDTO;
import com.bwagih.authservice.service.AuthService;
import com.bwagih.authservice.utils.APIResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Generate token on user login")
    @PostMapping("/login")
    public ResponseEntity<APIBusinessLogicResponse<LoginResponseDTO>> login(@RequestBody LoginRequestDTO loginRequestDTO) {

        Optional<String> tokenOptional = authService.authenticate(loginRequestDTO);

        if (tokenOptional.isEmpty()) {
            return APIResponseHandler.createResponse(
                    HttpStatus.FORBIDDEN,
                    APIBusinessLogicResponse.error("" + HttpStatus.FORBIDDEN.value(), "Invalid credentials")
            );
        }

        String token = tokenOptional.get();

        return APIResponseHandler.createResponse(
                HttpStatus.OK,
                APIBusinessLogicResponse.success(new LoginResponseDTO(token))
        );


    }

    @Operation(summary = "Validate Token")
    @GetMapping("/validate")
    public ResponseEntity<APIBusinessLogicResponse<Void>>  validateToken(@RequestHeader("Authorization") String authHeader) {

        // Authorization: Bearer <token>
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return APIResponseHandler.createResponse(
                    HttpStatus.UNAUTHORIZED,
                    APIBusinessLogicResponse.error("" + HttpStatus.UNAUTHORIZED.value(), "session is not valid..")
            );

        }

        if (authService.validateToken(authHeader.substring(7))) {
            return APIResponseHandler.createResponse(
                    HttpStatus.OK,
                    APIBusinessLogicResponse.success("0001", "session is valid..")
            );
        }

       return APIResponseHandler.createResponse(
                HttpStatus.UNAUTHORIZED,
                APIBusinessLogicResponse.error("ERR001" + HttpStatus.UNAUTHORIZED.value(), "session is not valid..")
        );

    }

}
