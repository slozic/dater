package com.slozic.dater.services.images;

import com.slozic.dater.dto.ImageParameters;
import com.slozic.dater.exceptions.DateImageException;
import com.slozic.dater.services.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class DateImageStorageStrategy implements ImageStorageStrategy {

    @Value("${date.images.max-count}")
    private int MAX_IMAGES_PER_DATE;
    @Value("${date.images.location}")
    private String imageLocation;
    @Value("${date.images.width}")
    private int imageWidth;
    @Value("${date.images.height}")
    private int imageHeight;
    @Value("${date.images.resize-type}")
    private String imageType;

    @Autowired
    private ImageStorageService<MultipartFile, ImageParameters, String, byte[]> imageStorageService;

    @Override
    public String storeImage(MultipartFile image) {
        return (String) imageStorageService.storeImage(image, getParameters());
    }

    @Override
    public byte[] loadImage(String imagePath) {
        byte[] imageBytes = imageStorageService.loadImage(imagePath);
        return imageStorageService.resizeImage(imageBytes, getParameters(imagePath));
    }

    @Override
    public void validate(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new DateImageException("No Images provided for Date Event");
        }

        if (images.size() > MAX_IMAGES_PER_DATE) {
            throw new DateImageException("You can have only up to " + MAX_IMAGES_PER_DATE + " images per date event!");
        }

        for (MultipartFile image : images) {
            if (!image.getContentType().equals(MediaType.IMAGE_JPEG_VALUE) && !image.getContentType().equals(MediaType.IMAGE_PNG_VALUE)) {
                throw new DateImageException("Unsupported file type " + image.getContentType());
            }
        }
    }

    @Override
    public void deleteImage(String imagePath) {
        imageStorageService.deleteImage(imagePath);
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
