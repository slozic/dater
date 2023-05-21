package com.slozic.dater.dto.request;

public record UserRegistrationRequest(String firstName, String lastName, String username, String password, String email, String birthday) {
}
