package com.slozic.dater.controllers;

import com.slozic.dater.dto.UserDto;
import com.slozic.dater.dto.request.UpdateUserProfileRequest;
import com.slozic.dater.dto.request.UserRegistrationRequest;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.services.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UsersController {
    private final UserService userService;

    @GetMapping
    public UserDto getCurrentAuthenticatedUser() throws UnauthorizedException {
        return userService.getCurrentAuthenticatedUser();
    }
    @PostMapping("/registration")
    public ResponseEntity<?> register(@Valid @RequestBody final UserRegistrationRequest request) {
        userService.doUserRegistration(request);
        return ResponseEntity.status(HttpStatus.OK).body("Successfully registered!");
    }

    @PutMapping("/profile")
    public UserDto updateProfile(@RequestBody final UpdateUserProfileRequest request) throws UnauthorizedException {
        return userService.updateCurrentUser(request);
    }

}
