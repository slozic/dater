package com.slozic.dater.services.images;

import com.slozic.dater.dto.ImageParameters;
import com.slozic.dater.exceptions.UserProfileImageException;
import com.slozic.dater.services.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class ProfileImageStorageStrategy implements ImageStorageStrategy {

    @Value("${user.images.max-count}")
    private int MAX_IMAGES_PER_USER_PROFILE;
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

    @Override
    public void validate(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new UserProfileImageException("No Images provided.");
        }

        if (images.size() > MAX_IMAGES_PER_USER_PROFILE) {
            throw new UserProfileImageException("You can have only up to " + MAX_IMAGES_PER_USER_PROFILE + " images per user profile!");
        }

        for (MultipartFile image : images) {
            if (!image.getContentType().equals(MediaType.IMAGE_JPEG_VALUE) && !image.getContentType().equals(MediaType.IMAGE_PNG_VALUE)) {
                throw new UserProfileImageException("Unsupported file type " + image.getContentType());
            }
        }
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
