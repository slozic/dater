package com.slozic.dater.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtils {
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";
    private static final long ACCESS_TOKEN_VALIDITY_SECONDS = 60L * 60L * 6L;
    private static final long REFRESH_TOKEN_VALIDITY_SECONDS = 60L * 60L * 24L * 30L;
    private String secretKey;

    public JWTUtils(String secretKey) {
        this.secretKey = secretKey;
    }

/*    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }*/

    public String getAccessTokenSubject(String token) {
        final Claims body = parseToken(token).getBody();
        validateTokenType(body, ACCESS_TOKEN_TYPE);
        return body.getSubject();
    }

    public String getRefreshTokenSubject(String token) {
        final Claims body = parseToken(token).getBody();
        validateTokenType(body, REFRESH_TOKEN_TYPE);
        return body.getSubject();
    }

    private Jws<Claims> parseToken(final String token) {
        final JwtParser jwtParser = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build();
        return jwtParser.parseClaimsJws(token);
    }

    private void validateTokenType(final Claims body, final String expectedType) {
        final Object tokenType = body.get(TOKEN_TYPE_CLAIM);
        if (!expectedType.equals(tokenType)) {
            throw new JwtException("Invalid token type.");
        }
    }

    public String generateAccessToken(final Map<String, Object> claims, final String subject) {
        final Map<String, Object> accessClaims = new HashMap<>(claims);
        accessClaims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);
        return Jwts.builder()
                .setClaims(accessClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(Date.from(new Date().toInstant().plusSeconds(ACCESS_TOKEN_VALIDITY_SECONDS)))
                .signWith((Keys.hmacShaKeyFor(secretKey.getBytes())), SignatureAlgorithm.HS512).compact();
    }

    public String generateRefreshToken(final String subject) {
        final Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "Dater App");
        claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(Date.from(new Date().toInstant().plusSeconds(REFRESH_TOKEN_VALIDITY_SECONDS)))
                .signWith((Keys.hmacShaKeyFor(secretKey.getBytes())), SignatureAlgorithm.HS512).compact();
    }
}
