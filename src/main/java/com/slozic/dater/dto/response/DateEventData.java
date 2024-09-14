package com.slozic.dater.dto.response;

public record DateEventData(String id, String title, String location, String description, String dateOwner, String dateOwnerId, String dateJoiner, String scheduledTime, String joinStatus) {
}
