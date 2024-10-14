package com.slozic.dater.controllers;

import com.slozic.dater.dto.response.userprofile.ProfileImageCreatedResponse;
import com.slozic.dater.dto.response.userprofile.ProfileImageResponse;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.images.ProfileImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/images")
@RequiredArgsConstructor
@Slf4j
public class ProfileImageController {
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;
    private final ProfileImageService profileImageService;

    @PostMapping
    public ProfileImageCreatedResponse createUserProfileImage(@RequestParam("files") List<MultipartFile> images) {
        UUID authenticatedUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return profileImageService.createProfileImages(authenticatedUser.toString(), images);
    }

    @GetMapping
    public ProfileImageResponse getUserProfileImages() {
        UUID authenticatedUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return profileImageService.getProfileImages(authenticatedUser.toString());
    }

    @DeleteMapping("/{imageId}")
    public Boolean deleteUserProfileImage(@PathVariable("imageId") String imageId) {
        UUID authenticatedUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return profileImageService.deleteImage(authenticatedUser.toString(), imageId);
    }

}
