package com.slozic.dater.auth;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CustomJdbcUserDetailsManager extends JdbcUserDetailsManager {

    private String userByEmailQuery = "select id, username, password, email, enabled from users where email = ?";

    protected ApplicationUser loadUserByEmail(String email) {
        final List<ApplicationUser> userDetailsList =
                this.getJdbcTemplate().query(this.getUserByEmailQuery(), this::mapToUser, new Object[]{email});
        if (userDetailsList.size() == 0) {
            this.logger.debug("Query returned no results for user '" + email + "'");
            throw new UsernameNotFoundException(
                    this.messages.getMessage("JdbcDaoImpl.notFound", new Object[]{email}, "Username {0} not found"));
        }
        return userDetailsList.get(0);
    }

    public String getUserByEmailQuery() {
        return this.userByEmailQuery;
    }

    private ApplicationUser mapToUser(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString(1);
        String userName = rs.getString(2);
        String password = rs.getString(3);
        String email = rs.getString(4);
        boolean enabled = rs.getBoolean(5);
        boolean accLocked = false;
        boolean accExpired = false;
        boolean credsExpired = false;
        if (rs.getMetaData().getColumnCount() > 5) {
            accLocked = rs.getBoolean(6);
            accExpired = rs.getBoolean(7);
            credsExpired = rs.getBoolean(8);
        }
        return new ApplicationUser(id, userName, password, email, enabled, !accExpired, !credsExpired, !accLocked, AuthorityUtils.NO_AUTHORITIES);
    }
}
