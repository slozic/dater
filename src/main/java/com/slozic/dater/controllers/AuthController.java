package com.slozic.dater.controllers;

import com.slozic.dater.dto.request.RefreshTokenRequest;
import com.slozic.dater.dto.response.TokenRefreshResponse;
import com.slozic.dater.security.jwt.JWTUtils;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JWTUtils jwtUtils;

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshAccessToken(@RequestBody final RefreshTokenRequest request) {
        if (request == null || request.refreshToken() == null || request.refreshToken().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        final String rawRefreshToken = request.refreshToken().replace("Bearer ", "").trim();
        try {
            final String subject = jwtUtils.getRefreshTokenSubject(rawRefreshToken);
            final String accessToken = jwtUtils.generateAccessToken(java.util.Map.of("iss", "Dater App"), subject);
            final String rotatedRefreshToken = jwtUtils.generateRefreshToken(subject);
            return ResponseEntity.ok(new TokenRefreshResponse(
                    "Bearer " + accessToken,
                    "Bearer " + rotatedRefreshToken
            ));
        } catch (JwtException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
