package com.slozic.dater.dto.response.userprofile;

import java.util.List;

public record UserImageCreatedResponse(String userId, List<String> imageIds) {
}
