package com.slozic.dater.services.images.strategy;

import com.slozic.dater.dto.ImageParameters;
import com.slozic.dater.dto.Result;
import com.slozic.dater.exceptions.dateimage.DateImageException;
import com.slozic.dater.models.DateImage;
import com.slozic.dater.repositories.DateImageRepository;
import com.slozic.dater.services.images.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Component
public class DateImageStorageStrategy implements ImageStorageStrategy<Result<byte[], String>> {

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
    private ImageStorageService<MultipartFile, String, Result<byte[], String>> imageStorageService;
    @Autowired
    private DateImageRepository dateImageRepository;

    @Override
    public String storeImage(MultipartFile image) {
        return imageStorageService.storeImage(image, getParameters());
    }

    @Override
    public Result<byte[], String> loadImage(String imagePath) {
        return imageStorageService.loadImage(imagePath);
    }

    @Override
    public Result<byte[], String> loadResizedImage(String imagePath) {
        Result<byte[], String> result = loadImage(imagePath);
        if (result.isSuccess()) {
            return imageStorageService.resizeImage(result, getParameters(imagePath));
        }
        return result;
    }

    @Override
    public void validate(List<MultipartFile> images, String dateId) {
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

        List<DateImage> dateImages = dateImageRepository.findAllByDateId(UUID.fromString(dateId));
        int emptyImageSlots = Math.abs(dateImages.size() - MAX_IMAGES_PER_DATE);

        if(emptyImageSlots < images.size()){
            throw new DateImageException("You can have only up to 3 images per date event! Available empty slots left: " + emptyImageSlots);
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
