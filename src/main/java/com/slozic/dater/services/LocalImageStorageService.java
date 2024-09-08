package com.slozic.dater.services;

import com.slozic.dater.exceptions.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.random.RandomGenerator;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalImageStorageService implements ImageStorageService {

    private static final String DEFAULT_IMAGES_LOCATION = "C:\\Users\\sly-x\\projects\\spring\\dater-images";
    private static final int DEFAULT_IMAGE_RESIZE_WIDTH_HEIGHT = 400;
    private static final String DEFAULT_IMAGE_RESIZE_TYPE = "jpg";

    @Override
    public String storeImage(final MultipartFile image) {
        if (image != null) {
            return writeImageToDisk(image);
        }
        return "";
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
        return getImageBytes(imagePath, true);
    }

    private byte[] getImageBytes(String imagePath, boolean shouldResize) {
        File file = new File(imagePath);
        byte[] imageBytes;

        try {
            imageBytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new FileStorageException("Could not load image " + imagePath + ". Please try again!", e);
        }
        if (shouldResize) {
            imageBytes = resizeImage(imageBytes, imagePath);
        }
        return imageBytes;
    }

    private byte[] resizeImage(byte[] imageBytes, String imagePath) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(new ByteArrayInputStream(imageBytes))
                    .size(DEFAULT_IMAGE_RESIZE_WIDTH_HEIGHT, DEFAULT_IMAGE_RESIZE_WIDTH_HEIGHT)
                    .outputFormat(DEFAULT_IMAGE_RESIZE_TYPE)
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new FileStorageException("Could not load image " + imagePath + ". Please try again!", e);
        }
    }
}
