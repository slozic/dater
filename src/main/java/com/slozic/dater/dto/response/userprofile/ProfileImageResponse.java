package com.slozic.dater.dto.response.userprofile;

import java.util.List;

public record ProfileImageResponse(List<ProfileImageData> profileImageData, String userId) {
}
