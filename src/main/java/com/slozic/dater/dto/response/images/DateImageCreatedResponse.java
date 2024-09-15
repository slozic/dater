package com.slozic.dater.dto.response.images;

import java.util.List;

public record DateImageCreatedResponse(String dateId, List<String> imageIds) {
}
