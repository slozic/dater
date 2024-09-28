package com.slozic.dater.services.images;

import com.slozic.dater.dto.ImageParameters;
import com.slozic.dater.services.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ProfileImageStorageStrategy implements ImageStorageStrategy {
    @Value("${user.images.location}")
    private String imageLocation;
    @Value("${user.images.width}")
    private int imageWidth;
    @Value("${user.images.height}")
    private int imageHeight;
    @Value("${user.images.resize-type}")
    private String imageType;
    @Autowired
    private ImageStorageService imageStorageService;

    @Override
    public String storeImage(MultipartFile image) {
        return imageStorageService.storeImage(image, getParameters());
    }

    @Override
    public byte[] loadImage(String imagePath) {
        byte[] imageBytes = imageStorageService.loadImage(imagePath);
        return imageStorageService.resizeImage(imageBytes, getParameters(imagePath));
    }

    private ImageParameters getParameters() {
        return new ImageParameters(
                imageLocation,
                imageWidth,
                imageHeight,
                imageType);
    }

    private ImageParameters getParameters(String location) {
        return new ImageParameters(
                location,
                imageWidth,
                imageHeight,
                imageType);
    }
}
