package com.slozic.dater.services.images;

import com.slozic.dater.dto.enums.ImageCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ImageStorageStrategyFactory {
    private final DateImageStorageStrategy dateImageStorageStrategy;
    private final UserImageStorageStrategy userImageStorageStrategy;

    public ImageStorageStrategy getStrategy(ImageCategory imageCategory) {
        if (imageCategory.equals(ImageCategory.DATE)) {
            return dateImageStorageStrategy;
        } else if (imageCategory.equals(ImageCategory.USER)) {
            return userImageStorageStrategy;
        }
        throw new IllegalArgumentException("Invalid image category");
    }
}
