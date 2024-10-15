package com.slozic.dater.services.images;

import com.slozic.dater.dto.ImageParameters;
import com.slozic.dater.dto.Result;
import com.slozic.dater.exceptions.FileStorageException;
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
public class LocalImageStorageService implements ImageStorageService<MultipartFile, String, Result<byte[], String>> {
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

    private File getFile(final ImageParameters parameters, final File imageDir) {
        return new File(imageDir.getPath() + "\\" +
                System.currentTimeMillis() +
                RandomGenerator.getDefault().nextInt() + "." +
                parameters.type());
    }

    @Override
    public Result<byte[], String> loadImage(final String imagePath) {
        try {
            byte[] imageBytes = getImageBytes(imagePath);
            return new Result<>(imageBytes, imagePath);
        } catch (IOException e) {
            log.error("Problem occurred with loading image {}, {}: ", imagePath, e.getMessage());
            return new Result<>(null, imagePath, e.getMessage());
        }
    }

    private byte[] getImageBytes(final String imagePath) throws IOException {
        File file = new File(imagePath);
        return Files.readAllBytes(file.toPath());
    }

    @Override
    public Result<byte[], String> resizeImage(final Result<byte[], String> loadResult, final ImageParameters parameters) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(new ByteArrayInputStream(loadResult.getPayload()))
                    .size(parameters.width(), parameters.height())
                    .outputFormat(parameters.type())
                    .toOutputStream(outputStream);
            return new Result<>(outputStream.toByteArray(), parameters.location());
        } catch (IOException e) {
            log.error("Problem occurred with resizing image: {}, {}", loadResult.getParameters(), e.getMessage());
            return new Result<>(new byte[]{}, loadResult.getParameters(), e.getMessage());
        }
    }

    @Override
    public void deleteImage(final String imagePath) {
        File file = new File(imagePath);
        boolean isDeleted = false;

        if (file.exists()) {
            isDeleted = file.delete();
        }

        if (!isDeleted) {
            log.error("Could not delete file under the path {}", imagePath);
        }
    }
}
