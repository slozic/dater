package com.slozic.dater.services;

import com.slozic.dater.dto.ImageParameters;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    public String storeImage(MultipartFile image, ImageParameters parameters);

    public byte[] loadImage(String imagePath);

    public byte[] resizeImage(byte[] imageBytes, ImageParameters parameters);
}
