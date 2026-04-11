package com.slozic.dater.services.user;

import com.slozic.dater.dto.UserDto;
import com.slozic.dater.dto.response.PublicProfileResponse;
import com.slozic.dater.dto.response.userprofile.ProfileImageResponse;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.images.ProfileImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicProfileService {
    private final UserService userService;
    private final ProfileImageService profileImageService;
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;
    private final UserModerationService userModerationService;

    public PublicProfileResponse getPublicProfile(String userId) {
        final UUID currentUserId = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        final UUID profileUserId = UUID.fromString(userId);
        userModerationService.assertUsersNotBlocked(
                currentUserId,
                profileUserId,
                "You cannot view this profile because one of you has blocked the other user."
        );
        UserDto userDto = userService.getUserById(userId);
        ProfileImageResponse profileImages = profileImageService.getProfileImages(userId);
        return new PublicProfileResponse(
                userDto.id(),
                userDto.username(),
                userDto.firstName() + " " + userDto.lastName(),
                userDto.gender(),
                profileImages.profileImageData());
    }
}
