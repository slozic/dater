package com.slozic.dater.controllers;

import com.slozic.dater.dto.request.ReportUserRequest;
import com.slozic.dater.dto.response.UserModerationActionResponse;
import com.slozic.dater.services.user.UserModerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/{id}/moderation")
@RequiredArgsConstructor
public class UserModerationController {
    private final UserModerationService userModerationService;

    @PostMapping("/block")
    public UserModerationActionResponse blockUser(@PathVariable("id") final String userId) {
        return userModerationService.blockUser(userId);
    }

    @PostMapping("/report")
    public UserModerationActionResponse reportUser(
            @PathVariable("id") final String userId,
            @Valid @RequestBody final ReportUserRequest request
    ) {
        return userModerationService.reportUser(userId, request);
    }

    @PostMapping("/report-and-block")
    public UserModerationActionResponse reportAndBlockUser(
            @PathVariable("id") final String userId,
            @Valid @RequestBody final ReportUserRequest request
    ) {
        return userModerationService.reportAndBlockUser(userId, request);
    }
}
