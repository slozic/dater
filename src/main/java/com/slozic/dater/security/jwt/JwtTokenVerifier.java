package com.slozic.dater.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtTokenVerifier extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
            final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain
    ) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        if (StringUtils.isEmpty(authorizationHeader) || !authorizationHeader.startsWith("Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            final String token = authorizationHeader.replace("Bearer", "");
            final String secretKey = "secretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkey";

            final JwtParser jwtParser = Jwts.parserBuilder()
                                            .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                                            .build();
            final Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
            final Claims body = claimsJws.getBody();
            final String subject = body.getSubject();

            Authentication authentication = new UsernamePasswordAuthenticationToken(subject, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException e) {
            throw new IllegalStateException(String.format("Authentication failed during token validation"));
        }

        filterChain.doFilter(request, response);
    }
}
