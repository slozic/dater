package com.slozic.dater.controllers;

import com.slozic.dater.dto.response.userprofile.UserImageCreatedResponse;
import com.slozic.dater.dto.response.userprofile.UserImageResponse;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.UserImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserImageController {
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;
    private final UserImageService userImageService;

    @PostMapping("/images")
    public UserImageCreatedResponse createUserImage(@RequestParam("files") List<MultipartFile> images) {
        UUID authenticatedUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return userImageService.createUserImages(authenticatedUser, images);
    }

    @GetMapping("/{id}/images")
    public UserImageResponse getUserImages(@PathVariable("id") String userId) {
        UUID authenticatedUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return userImageService.getUserImages(userId);
    }

}
