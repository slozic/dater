package com.slozic.dater.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slozic.dater.auth.ApplicationUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;
    private JWTUtils jwtUtils;

    public JwtUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager, JWTUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

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
        String subject = ((ApplicationUser) authResult.getPrincipal()).id();
        Map<String, Object> claims = getClaims(authResult);
        String token = jwtUtils.generateToken(claims, subject);
        response.addHeader("Authorization", "Bearer " + token);
        response.addHeader("Access-Control-Expose-Headers", "Authorization");
    }

    private Map<String, Object> getClaims(Authentication authResult) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "Dater App");
        claims.put("authorities", authResult.getAuthorities());
        return claims;
    }
}
