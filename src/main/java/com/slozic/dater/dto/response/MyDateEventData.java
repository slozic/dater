package com.slozic.dater.dto.response;

public record MyDateEventData(String id, String title, String location, String description, String dateJoiner, String scheduledTime, String joinStatus, boolean dateCreator) {
}
