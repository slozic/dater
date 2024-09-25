package com.slozic.dater.dto.response.userprofile;

import java.util.List;

public record UserImageResponse(List<UserImageData> userImageDataList, String userId) {
}
