package com.slozic.dater.dto;

import java.util.List;

public record UserImageCreatedResponse(String userId, List<String> imageIds) {
}
