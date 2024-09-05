package com.slozic.dater.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ImageStorageService {
    public void storeImage(Optional<MultipartFile> image);

    public void loadImage(String imagePath);
}
