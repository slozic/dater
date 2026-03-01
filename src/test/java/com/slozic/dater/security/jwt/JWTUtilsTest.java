package com.slozic.dater.security.jwt;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JWTUtilsTest {
    private static final String SIGNING_KEY = "secretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkey";

    @Test
    void getAccessTokenSubject_shouldReturnSubjectForAccessToken() {
        final JWTUtils jwtUtils = new JWTUtils(SIGNING_KEY);
        final String subject = "user-1";
        final String token = jwtUtils.generateAccessToken(Collections.emptyMap(), subject);

        final String actualSubject = jwtUtils.getAccessTokenSubject(token);

        assertThat(actualSubject).isEqualTo(subject);
    }

    @Test
    void getRefreshTokenSubject_shouldReturnSubjectForRefreshToken() {
        final JWTUtils jwtUtils = new JWTUtils(SIGNING_KEY);
        final String subject = "user-2";
        final String token = jwtUtils.generateRefreshToken(subject);

        final String actualSubject = jwtUtils.getRefreshTokenSubject(token);

        assertThat(actualSubject).isEqualTo(subject);
    }

    @Test
    void getAccessTokenSubject_shouldThrowForRefreshToken() {
        final JWTUtils jwtUtils = new JWTUtils(SIGNING_KEY);
        final String refreshToken = jwtUtils.generateRefreshToken("user-3");

        assertThrows(JwtException.class, () -> jwtUtils.getAccessTokenSubject(refreshToken));
    }

    @Test
    void getRefreshTokenSubject_shouldThrowForAccessToken() {
        final JWTUtils jwtUtils = new JWTUtils(SIGNING_KEY);
        final String accessToken = jwtUtils.generateAccessToken(Collections.emptyMap(), "user-4");

        assertThrows(JwtException.class, () -> jwtUtils.getRefreshTokenSubject(accessToken));
    }
}
