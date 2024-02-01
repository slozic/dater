package com.slozic.dater.services;

import com.slozic.dater.dto.UserDto;
import com.slozic.dater.dto.request.UserRegistrationRequest;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.models.User;
import com.slozic.dater.repositories.UserRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    public UserDto getCurrentAuthenticatedUser() throws UnauthorizedException {
        final User user = userRepository.findOneById(jwtAuthenticatedUserService.getCurrentUserOrThrow())
                .orElseThrow(() -> new RuntimeException("User not found!"));
        return UserDto.from(user);
    }

    public UserDto doUserRegistration(final UserRegistrationRequest request) {
        final Optional<User> userExists = userRepository.findOneByEmail(request.email());
        if (userExists.isPresent()) {
            throw new IllegalArgumentException("Cannot create user!");
        }

        User user = User.fromUserRegistrationRequest(request)
                .toBuilder()
                .password(bCryptPasswordEncoder.encode(request.password()))
                .build();
        userRepository.save(user);
        return UserDto.from(user);
    }
}
