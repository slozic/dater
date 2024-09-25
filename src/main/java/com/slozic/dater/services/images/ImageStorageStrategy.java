package com.slozic.dater.services.images;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageStrategy {

    public String storeImage(MultipartFile image);

    byte[] loadImage(String imagePath);
}
