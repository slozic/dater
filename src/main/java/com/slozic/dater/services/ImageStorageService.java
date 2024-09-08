package com.slozic.dater.services;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    public String storeImage(MultipartFile image);
    public byte[] loadImage(String imagePath);
}
