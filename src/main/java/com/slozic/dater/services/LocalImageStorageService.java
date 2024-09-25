package com.slozic.dater.services;

import com.slozic.dater.exceptions.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.random.RandomGenerator;

@Service
@Slf4j
public class LocalImageStorageService implements ImageStorageService {

    @Value("${date.images.location}")
    private String DEFAULT_IMAGES_LOCATION;
    @Value("${date.images.width}")
    private int DEFAULT_IMAGE_RESIZE_WIDTH;
    @Value("${date.images.height}")
    private int DEFAULT_IMAGE_RESIZE_HEIGHT;
    @Value("${date.images.resize-type}")
    private String DEFAULT_IMAGE_RESIZE_TYPE;

    @Override
    public String storeImage(final MultipartFile image) {
        if (image != null) {
            return writeImageToDisk(image);
        }
        return StringUtils.EMPTY;
    }

    private String writeImageToDisk(final MultipartFile image) {
        File imageDir = new File(DEFAULT_IMAGES_LOCATION);
        File file = new File(imageDir.getPath() + "\\" + System.currentTimeMillis() + RandomGenerator.getDefault().nextInt() + "." + DEFAULT_IMAGE_RESIZE_TYPE);

        try (OutputStream os = new FileOutputStream(file)) {
            os.write(image.getBytes());
        } catch (IOException ex) {
            throw new FileStorageException("Could not store image " + image.getName() + ". Please try again!", ex);
        }
        return file.getPath();
    }

    @Override
    public byte[] loadImage(String imagePath) {
        byte[] imageBytes = getImageBytes(imagePath);
        return resizeImage(imageBytes, imagePath);
    }

    private byte[] getImageBytes(String imagePath) {
        File file = new File(imagePath);
        byte[] imageBytes;

        try {
            imageBytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new FileStorageException("Could not load image " + imagePath + ". Please try again!", e);
        }
        return imageBytes;
    }

    private byte[] resizeImage(byte[] imageBytes, String imagePath) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(new ByteArrayInputStream(imageBytes))
                    .size(DEFAULT_IMAGE_RESIZE_WIDTH, DEFAULT_IMAGE_RESIZE_HEIGHT)
                    .outputFormat(DEFAULT_IMAGE_RESIZE_TYPE)
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new FileStorageException("Could not load image " + imagePath + ". Please try again!", e);
        }
    }
}
