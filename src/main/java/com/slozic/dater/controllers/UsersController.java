package com.slozic.dater.controllers;

import com.slozic.dater.dto.UserDto;
import com.slozic.dater.dto.request.UserRegistrationRequest;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.models.User;
import com.slozic.dater.repositories.UserRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UsersController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping
    public UserDto getCurrentAuthenticatedUser() throws UnauthorizedException {
        final User user = userRepository.findOneById(JwtAuthenticatedUserService.getCurrentUserOrThrow())
                                        .orElseThrow(() -> new RuntimeException("User not found!"));
        return UserDto.from(user);
    }

    @PostMapping("/registration")
    @Transactional
    public ResponseEntity<?> register(@RequestBody final UserRegistrationRequest request){
        User user = User.fromUserRegistrationRequest(request)
                .toBuilder()
                .password(bCryptPasswordEncoder.encode(request.password()))
                .build();
        final Optional<User> userExists = userRepository.findOneByEmail(request.email());
        if (userExists.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.OK).body("Successfully registered!");
    }

}
