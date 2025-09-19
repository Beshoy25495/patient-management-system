package com.bwagih.authservice.service;

import com.bwagih.authservice.dto.LoginRequestDTO;
import com.bwagih.authservice.utils.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {
        log.info("Authenticating user: {}", loginRequestDTO.getEmail());
        Optional<String> token = userService.findByEmail(loginRequestDTO.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(),
                        u.getPassword()))
                .map(u -> jwtUtil.generateToken(u.getEmail(), u.getRole()));

        return token;
    }

    public boolean validateToken(String token) {
        log.info("Validating token: {}", token);
        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (JwtException e) {
            log.error("Invalid token: {}", token);
            return false;
        }
    }


}
