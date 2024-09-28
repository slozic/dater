package com.slozic.dater.dto.response.userprofile;

import java.util.List;

public record ProfileImageCreatedResponse(String userId, List<String> imageIds) {
}
