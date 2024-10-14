package com.slozic.dater.dto.response.images;

public record DateImageData(byte[] image, String id, String errorMessage) {

    public DateImageData(byte[] image, String id) {
        this(image, id, null);
    }

    public DateImageData(byte[] image, String id, String errorMessage) {
        this.image = image;
        this.id = id;
        this.errorMessage = errorMessage;
    }
}
