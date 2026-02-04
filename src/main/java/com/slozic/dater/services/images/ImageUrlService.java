package com.slozic.dater.services.images;

import com.slozic.dater.dto.enums.ImageCategory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Path;

@Service
public class ImageUrlService {
    public String buildUrl(ImageCategory category, String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }
        String fileName = Path.of(imagePath).getFileName().toString();
        String segment = category == ImageCategory.USER ? "user" : "date";
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/media/")
                .path(segment + "/")
                .path(fileName)
                .toUriString();
    }
}
