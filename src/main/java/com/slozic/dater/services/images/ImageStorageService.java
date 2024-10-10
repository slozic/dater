package com.slozic.dater.services.images;

public interface ImageStorageService<ImageType, ImageParamType, ReturnType, LoadType> {
    public ReturnType storeImage(ImageType imageType, ImageParamType parameters);

    public LoadType loadImage(String imagePath);

    public LoadType resizeImage(LoadType loadType, ImageParamType parameters);

    void deleteImage(String imagePath);
}
