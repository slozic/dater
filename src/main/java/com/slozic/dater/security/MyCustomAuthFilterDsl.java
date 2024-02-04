package com.slozic.dater.security;

import com.slozic.dater.security.jwt.JwtTokenVerifier;
import com.slozic.dater.security.jwt.JwtUsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.stereotype.Component;

@Component
public class MyCustomAuthFilterDsl extends AbstractHttpConfigurer<MyCustomAuthFilterDsl, HttpSecurity> {

    @Value("${jwt.signing-key}")
    private String secretKey;

    @Override
    public void init(final HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf().disable();
    }
    @Override
    public void configure(final HttpSecurity http) {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        http.addFilter(new JwtUsernamePasswordAuthenticationFilter(authenticationManager, secretKey));
        http.addFilterAfter(new JwtTokenVerifier(secretKey), JwtUsernamePasswordAuthenticationFilter.class);
    }

}
