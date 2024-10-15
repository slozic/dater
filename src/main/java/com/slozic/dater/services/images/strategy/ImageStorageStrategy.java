package com.slozic.dater.services.images.strategy;

import com.slozic.dater.dto.Result;

import java.util.List;

public interface ImageStorageStrategy<T, R extends Result<?, ?>> {

    String storeImage(T image);

    R loadImage(String imagePath);

    R loadResizedImage(String imagePath);

    void validate(List<T> images, String param);

    void deleteImage(String imagePath);
}
