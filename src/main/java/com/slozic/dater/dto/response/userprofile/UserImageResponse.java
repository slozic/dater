package com.slozic.dater.dto.response.userprofile;

import java.util.List;

public record UserImageResponse(List<UserImageData> userImageData, String userId) {
}
