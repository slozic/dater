package com.slozic.dater.testconfig;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestComponent;

import java.util.Collections;
import java.util.Date;

@TestComponent
public class JwsBuilder {
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    @Value("${jwt.signing-key}")
    private String secretKey;

    public String getJwt() {
        String token = Jwts.builder()
                .setSubject("aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5")
                .claim("authorities", Collections.EMPTY_LIST)
                .claim("token_type", ACCESS_TOKEN_TYPE)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(new Date().toInstant().plusSeconds(36000)))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
        return token;
    }

    public String getJwt(String userId) {
        String token = Jwts.builder()
                .setSubject(userId)
                .claim("authorities", Collections.EMPTY_LIST)
                .claim("token_type", ACCESS_TOKEN_TYPE)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(new Date().toInstant().plusSeconds(36000)))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
        return token;
    }

    public String getRefreshJwt(String userId) {
        String token = Jwts.builder()
                .setSubject(userId)
                .claim("token_type", REFRESH_TOKEN_TYPE)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(new Date().toInstant().plusSeconds(36000)))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
        return token;
    }
}
