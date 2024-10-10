package com.slozic.dater.services.images;

import com.slozic.dater.dto.ImageParameters;
import com.slozic.dater.exceptions.FileStorageException;
import com.slozic.dater.services.images.ImageStorageService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.random.RandomGenerator;

@Service
@Slf4j
public class LocalImageStorageService implements ImageStorageService<MultipartFile, ImageParameters, String, byte[]> {
    @Override
    public String storeImage(final MultipartFile image, final ImageParameters parameters) {
        if (image != null) {
            return writeImageToDisk(image, parameters);
        }
        return StringUtils.EMPTY;
    }

    private String writeImageToDisk(final MultipartFile image, final ImageParameters parameters) {
        File imageDir = new File(parameters.location());
        File file = getFile(parameters, imageDir);

        try (OutputStream os = new FileOutputStream(file)) {
            os.write(image.getBytes());
        } catch (IOException ex) {
            log.error("Could not store image " + image.getName() + "Reason: " + ex.getMessage());
            throw new FileStorageException("Could not store image " + image.getName() + ". Please try again!", ex);
        }
        return file.getPath();
    }

    private File getFile(ImageParameters parameters, File imageDir) {
        return new File(imageDir.getPath() + "\\" +
                System.currentTimeMillis() +
                RandomGenerator.getDefault().nextInt() + "." +
                parameters.type());
    }

    @Override
    public byte[] loadImage(String imagePath) {
        return getImageBytes(imagePath);
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

    @Override
    public byte[] resizeImage(byte[] imageBytes, ImageParameters parameters) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(new ByteArrayInputStream(imageBytes))
                    .size(parameters.width(), parameters.height())
                    .outputFormat(parameters.type())
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new FileStorageException("Could not load image " + parameters.location() + ". Please try again!", e);
        }
    }

    @Override
    public void deleteImage(String imagePath) {
        File file = new File(imagePath);
        boolean isDeleted = false;

        if (file.exists()) {
            isDeleted = file.delete();
        }

        if (!isDeleted) {
            log.error("Could not delete file under the path ", imagePath);
        }
    }
}
