package com.slozic.dater.controllers;

import com.slozic.dater.dto.response.PublicProfileResponse;
import com.slozic.dater.services.PublicProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/{id}/public-profile")
@RequiredArgsConstructor
@Slf4j
public class PublicProfileController {

    private final PublicProfileService publicProfileService;

    @GetMapping
    public PublicProfileResponse getPublicProfile(@PathVariable("id") String userId) {
        return publicProfileService.getPublicProfile(userId);
    }

}
