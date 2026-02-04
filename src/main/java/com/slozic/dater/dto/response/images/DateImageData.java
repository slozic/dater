package com.slozic.dater.dto.response.images;

public record DateImageData(String imageUrl, String id, String errorMessage) {

    public DateImageData(String imageUrl, String id) {
        this(imageUrl, id, null);
    }

    public DateImageData(String imageUrl, String id, String errorMessage) {
        this.imageUrl = imageUrl;
        this.id = id;
        this.errorMessage = errorMessage;
    }
}
