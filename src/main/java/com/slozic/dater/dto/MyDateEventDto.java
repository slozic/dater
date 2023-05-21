package com.slozic.dater.dto;

public record MyDateEventDto(String id, String title, String location, String description, String dateOwner, String dateJoiner, String scheduledTime, String joinStatus, boolean dateCreator) {
}
