package com.slozic.dater.dto.response;

import com.slozic.dater.dto.response.userprofile.ProfileImageData;

import java.util.List;

public record PublicProfileResponse(String userId, String username, String fullName, String gender,
                                    List<ProfileImageData> profileImageData) {
}
