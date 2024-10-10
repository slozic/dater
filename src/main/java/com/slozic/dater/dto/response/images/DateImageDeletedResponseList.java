package com.slozic.dater.dto.response.images;

import java.util.List;

public record DateImageDeletedResponseList(String dateId, List<String> imageIds) {
}
