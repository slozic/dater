package com.slozic.dater.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Repository("h2")
@RequiredArgsConstructor
public class H2ApplicationUserDaoImpl implements ApplicationUserDao {

    private final PasswordEncoder passwordEncoder;
    private final DataSource h2DataSource;

    @Override
    public Optional<ApplicationUser> selectApplicationUserByUsername(final String username) {
        return getUserByUsername(username).stream()
                .filter(applicationUser -> applicationUser.getUsername().equals(username))
                .findFirst();
    }

    private List<ApplicationUser> getUserByUsername(final String username){
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(h2DataSource);
        jdbcUserDetailsManager.setEnableGroups(false);
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
