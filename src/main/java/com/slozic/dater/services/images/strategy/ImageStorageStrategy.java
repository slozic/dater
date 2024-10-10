package com.slozic.dater.services.images.strategy;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageStorageStrategy {

    String storeImage(MultipartFile image);

    byte[] loadImage(String imagePath);

    void validate(List<MultipartFile> images);

    void deleteImage(String imagePath);
}
