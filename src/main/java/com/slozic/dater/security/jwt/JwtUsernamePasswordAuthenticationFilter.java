package com.slozic.dater.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Date;

@Slf4j
@AllArgsConstructor
public class JwtUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response)
            throws AuthenticationException {
        try {
            final UsernamePasswordAuthenticationRequest usernamePasswordAuthenticationRequest =
                    new ObjectMapper().readValue(request.getInputStream(), UsernamePasswordAuthenticationRequest.class);
            final Authentication authentication = new UsernamePasswordAuthenticationToken(usernamePasswordAuthenticationRequest.username(),
                                                                                          usernamePasswordAuthenticationRequest.password()
            );
            return authenticationManager.authenticate(authentication);
        } catch (IOException iex) {
            throw new RuntimeException(iex);
        }
    }

    @Override
    protected void successfulAuthentication(
            final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain, final Authentication authResult
    ) {
        String key = "secretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkey";
        final String token = Jwts.builder()
                                 .setSubject(authResult.getName())
                                 .claim("authorities", authResult.getAuthorities())
                                 .setIssuedAt(new Date())
                                 .setExpiration(Date.from(new Date().toInstant().plusSeconds(3600)))
                                 .signWith(Keys.hmacShaKeyFor(key.getBytes()))
                                 .compact();
        response.addHeader("Authorization", "Bearer " + token);
    }
}
