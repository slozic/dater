package com.slozic.dater.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("postgres")
public class PostgresApplicationUserDaoImpl implements ApplicationUserDao {

    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final CustomJdbcUserDetailsManager jdbcUserDetailsManager;

    public PostgresApplicationUserDaoImpl(
            final PasswordEncoder passwordEncoder, final JdbcTemplate jdbcTemplate
    ) {
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcUserDetailsManager = new CustomJdbcUserDetailsManager();
        this.jdbcUserDetailsManager.setJdbcTemplate(jdbcTemplate);
        this.jdbcUserDetailsManager.setEnableGroups(false);
    }

    @Override
    public Optional<ApplicationUser> selectApplicationUserByUsername(final String username) {
        return Optional.of(jdbcUserDetailsManager.loadUserByEmail(username));
    }

    private List<ApplicationUser> getUserByUsername(final String username){
        final UserDetails userDetails = jdbcUserDetailsManager.loadUserByUsername(username);
        return List.of(
                new ApplicationUser(
                        "",
                        userDetails.getUsername(),
                        passwordEncoder.encode(userDetails.getPassword()),
                        "",
                        userDetails.isAccountNonExpired(),
                        userDetails.isAccountNonLocked(),
                        userDetails.isCredentialsNonExpired(),
                        userDetails.isEnabled(),
                        List.of()
                )
        );
    }

}
