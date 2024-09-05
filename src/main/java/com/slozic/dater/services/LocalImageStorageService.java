package com.slozic.dater.services;

import com.slozic.dater.models.DateImage;
import com.slozic.dater.repositories.DateImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalImageStorageService implements ImageStorageService {

    private static final String DEFAULT_IMAGES_LOCATION = "C:\\Users\\sly-x\\projects\\spring\\dater-images";
    private static final int DEFAULT_IMAGE_RESIZE_WIDTH_HEIGHT = 400;
    private static final String DEFAULT_IMAGE_RESIZE_TYPE = "png";

    private final DateImageRepository dateImageRepository;

    @Override
    public void storeImage(Optional<MultipartFile> image) {
        if (image.isPresent()) {
            storeDateEventImage(image.get());
        }
    }

    private String storeDateEventImage(MultipartFile image) {
        MultipartFile multipartFile = image;
        File file = storeImageAsFile(multipartFile);
        return file.getPath();
    }

    private File storeImageAsFile(final MultipartFile image) {
        File imageDir = new File(DEFAULT_IMAGES_LOCATION);
        File file = new File(imageDir.getPath() + "\\" + System.currentTimeMillis() + RandomGenerator.getDefault().nextInt() + "." + DEFAULT_IMAGE_RESIZE_TYPE);
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    @Override
    public void loadImage(String imagePath) {
        File file = new File(imagePath);
        try {
            byte[] imageBytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] resizeImage(byte[] imageBytes) throws IOException {
        // Resize the image
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(new ByteArrayInputStream(imageBytes))
                    .size(DEFAULT_IMAGE_RESIZE_WIDTH_HEIGHT, DEFAULT_IMAGE_RESIZE_WIDTH_HEIGHT)
                    .outputFormat(DEFAULT_IMAGE_RESIZE_TYPE)
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        }
    }

    public byte[] getImageBytes(String dateId, boolean shouldResize) throws IOException {
        List<DateImage> dateImageList = dateImageRepository.findAllByDateId(UUID.fromString(dateId));
        DateImage dateImage = dateImageList.stream().findFirst().orElseThrow(() -> new RuntimeException());
        // Read the image file
        File file = new File(dateImage.getImagePath());
        byte[] imageBytes = Files.readAllBytes(file.toPath());

        if (shouldResize) {
            imageBytes = resizeImage(imageBytes);
        }
        return imageBytes;
    }
}
