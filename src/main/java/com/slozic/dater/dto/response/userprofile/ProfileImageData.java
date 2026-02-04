package com.slozic.dater.dto.response.userprofile;

public record ProfileImageData(String imageUrl, String id, String errorMessage) {
    public ProfileImageData(String imageUrl, String id) {
        this(imageUrl, id, null);
    }
}
