package com.slozic.dater.dto.request;

public record UpdateUserProfileRequest(String firstName, String lastName, String username, String birthday, String gender) {
}
