package com.slozic.dater.services.images.strategy;

import com.slozic.dater.dto.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageStorageStrategy<R extends Result<?, ?>> {

    String storeImage(MultipartFile image);

    R loadImage(String imagePath);
    R loadResizedImage(String imagePath);

    void validate(List<MultipartFile> images, String param);

    void deleteImage(String imagePath);
}
