package com.slozic.dater.services.user;

import com.slozic.dater.dto.UserDto;
import com.slozic.dater.dto.request.UpdateUserProfileRequest;
import com.slozic.dater.dto.request.UserRegistrationRequest;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.exceptions.user.UserNotFoundException;
import com.slozic.dater.models.User;
import com.slozic.dater.repositories.UserRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    public UserDto getCurrentAuthenticatedUser() throws UnauthorizedException {
        UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        final User user = userRepository.findOneById(currentUser)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentUser));
        return UserDto.from(user);
    }

    public UserDto getUserById(final String userId) throws UnauthorizedException {
        final User user = userRepository.findOneById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("User with id not found: " + userId));
        return UserDto.from(user);
    }

    @Transactional
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

    @Transactional
    public UserDto updateCurrentUser(final UpdateUserProfileRequest request) throws UnauthorizedException {
        UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        User user = userRepository.findOneById(currentUser)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentUser));

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            userRepository.findOneByUsername(request.username())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(user.getId())) {
                            throw new IllegalArgumentException("Username is already taken.");
                        }
                    });
            user.setUsername(request.username());
        }

        if (request.firstName() != null) {
            user.setFirstname(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastname(request.lastName());
        }
        if (request.birthday() != null) {
            user.setBirthday(LocalDate.parse(request.birthday(), DateTimeFormatter.ISO_LOCAL_DATE));
        }

        return UserDto.from(userRepository.save(user));
    }

}
