package com.slozic.dater.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("default")
@RequiredArgsConstructor
public class DefaultApplicationUserDaoImpl implements ApplicationUserDao{

    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<ApplicationUser> selectApplicationUserByUsername(final String username) {
        return getApplicationUsers().stream()
                .filter(applicationUser -> applicationUser.getUsername().equals(username))
                .findFirst();
    }

    private List<ApplicationUser> getApplicationUsers(){
        return List.of(
                new ApplicationUser(
                        List.of(),
                        passwordEncoder.encode("password"),
                        "slavko",
                        true,
                        true,
                        true,
                        true
                )
        );
    }

}
