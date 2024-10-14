package com.slozic.dater.services.images;

import com.slozic.dater.dto.ImageParameters;
import com.slozic.dater.dto.Result;

public interface ImageStorageService<ImageInput, Response, R extends Result<?, ?>> {
    public Response storeImage(ImageInput imageInput, ImageParameters parameters);

    public R loadImage(String imagePath);

    public R resizeImage(R loadResult, ImageParameters parameters);

    void deleteImage(String imagePath);
}
