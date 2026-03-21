package com.slozic.dater.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("postgres")
public class PostgresApplicationUserDaoImpl implements ApplicationUserDao {

    private final CustomJdbcUserDetailsManager jdbcUserDetailsManager;

    public PostgresApplicationUserDaoImpl(
            final JdbcTemplate jdbcTemplate
    ) {
        this.jdbcUserDetailsManager = new CustomJdbcUserDetailsManager();
        this.jdbcUserDetailsManager.setJdbcTemplate(jdbcTemplate);
        this.jdbcUserDetailsManager.setEnableGroups(false);
    }

    @Override
    public Optional<ApplicationUser> selectApplicationUserByEmail(final String email) {
        return Optional.of(jdbcUserDetailsManager.loadUserByEmail(email));
    }

}
