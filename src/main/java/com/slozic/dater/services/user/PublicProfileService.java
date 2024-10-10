package com.slozic.dater.services.user;

import com.slozic.dater.dto.UserDto;
import com.slozic.dater.dto.response.PublicProfileResponse;
import com.slozic.dater.dto.response.userprofile.ProfileImageResponse;
import com.slozic.dater.services.images.ProfileImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicProfileService {
    private final UserService userService;
    private final ProfileImageService profileImageService;

    public PublicProfileResponse getPublicProfile(String userId) {
        UserDto userDto = userService.getUserById(userId);
        ProfileImageResponse profileImages = profileImageService.getProfileImages(userId);
        return new PublicProfileResponse(userDto.id(), userDto.username(), userDto.firstName() + " " + userDto.lastName(), "", profileImages.profileImageData());
    }
}
