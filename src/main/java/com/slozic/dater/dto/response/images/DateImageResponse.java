package com.slozic.dater.dto.response.images;

import java.util.List;

public record DateImageResponse(List<DateImageData> dateImageData, String dateId) {
}
