package com.slozic.dater.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("postgres")
@RequiredArgsConstructor
public class PostgresApplicationUserDaoImpl implements ApplicationUserDao {

    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<ApplicationUser> selectApplicationUserByUsername(final String username) {
        return getUserByUsername(username)
                .stream()
                .filter(applicationUser -> applicationUser.getUsername().equals(username))
                .findFirst();
    }

    private List<ApplicationUser> getUserByUsername(final String username){
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager();
        jdbcUserDetailsManager.setJdbcTemplate(jdbcTemplate);
        jdbcUserDetailsManager.setEnableGroups(false);
        final UserDetails userDetails = jdbcUserDetailsManager.loadUserByUsername(username);
        return List.of(
                new ApplicationUser(
                        null,
                        passwordEncoder.encode(userDetails.getPassword()),
                        userDetails.getUsername(),
                        userDetails.isAccountNonExpired(),
                        userDetails.isAccountNonLocked(),
                        userDetails.isCredentialsNonExpired(),
                        userDetails.isEnabled()
                )
        );
    }
}
