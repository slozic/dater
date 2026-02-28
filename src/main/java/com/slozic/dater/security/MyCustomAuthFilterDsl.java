package com.slozic.dater.security;

import com.slozic.dater.security.jwt.JWTUtils;
import com.slozic.dater.security.jwt.JwtTokenVerifierFilter;
import com.slozic.dater.security.jwt.JwtUsernamePasswordAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyCustomAuthFilterDsl extends AbstractHttpConfigurer<MyCustomAuthFilterDsl, HttpSecurity> {
    private final JWTUtils jwtUtils;

    @Override
    public void init(final HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);
    }

    @Override
    public void configure(final HttpSecurity http) {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        http.addFilter(new JwtUsernamePasswordAuthenticationFilter(authenticationManager, jwtUtils));
        http.addFilterAfter(new JwtTokenVerifierFilter(jwtUtils), JwtUsernamePasswordAuthenticationFilter.class);
    }

}
