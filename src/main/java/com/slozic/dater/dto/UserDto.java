package com.slozic.dater.dto;

import com.slozic.dater.models.User;

public record UserDto(String id, String firstName, String lastName, String username, String email, String birthday) {
    public static UserDto from(final User user) {
        return new UserDto(user.getId().toString(), user.getFirstname(), user.getLastname(), user.getUsername(), user.getEmail(), user.getBirthday().toString());
    }
}
