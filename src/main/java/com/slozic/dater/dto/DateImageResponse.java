package com.slozic.dater.dto;

import java.util.List;

public record DateImageResponse(List<DateImageData> dateImageData, String dateId) {
}
