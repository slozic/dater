package com.slozic.dater.security;

import com.slozic.dater.security.jwt.JwtTokenVerifier;
import com.slozic.dater.security.jwt.JwtUsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

public class MyCustomAuthFilterDsl extends AbstractHttpConfigurer<MyCustomAuthFilterDsl, HttpSecurity> {

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
        http.addFilter(new JwtUsernamePasswordAuthenticationFilter(authenticationManager));
        http.addFilterAfter(new JwtTokenVerifier(), JwtUsernamePasswordAuthenticationFilter.class);
    }

    public static MyCustomAuthFilterDsl customAuthFilterDsl() {
        return new MyCustomAuthFilterDsl();
    }
}
