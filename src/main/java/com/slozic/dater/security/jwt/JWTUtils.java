package com.slozic.dater.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.util.Map;

public class JWTUtils {
    private static final long JWT_TOKEN_VALIDITY = 3600;
    private String secretKey;

    public JWTUtils(String secretKey) {
        this.secretKey = secretKey;
    }

/*    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }*/

    public String getTokenSubject(String token) {
        final JwtParser jwtParser = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build();
        final Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
        final Claims body = claimsJws.getBody();
        return body.getSubject();
    }

    public String generateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(Date.from(new Date().toInstant().plusSeconds(JWT_TOKEN_VALIDITY * 10)))
                .signWith((Keys.hmacShaKeyFor(secretKey.getBytes())), SignatureAlgorithm.HS512).compact();
    }

}
