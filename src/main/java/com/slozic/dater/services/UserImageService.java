package com.slozic.dater.services;

import com.slozic.dater.dto.UserImageDto;
import com.slozic.dater.dto.enums.ImageCategory;
import com.slozic.dater.dto.response.userprofile.UserImageCreatedResponse;
import com.slozic.dater.dto.response.userprofile.UserImageData;
import com.slozic.dater.dto.response.userprofile.UserImageResponse;
import com.slozic.dater.exceptions.UserImageException;
import com.slozic.dater.models.UserImage;
import com.slozic.dater.repositories.UserImageRepository;
import com.slozic.dater.services.images.ImageStorageStrategy;
import com.slozic.dater.services.images.ImageStorageStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserImageService {
    @Value("${user.images.max-count}")
    private int MAX_IMAGES_PER_USER_PROFILE;
    @Autowired
    private ImageStorageStrategyFactory imageStorageStrategyFactory;
    @Autowired
    private UserImageRepository userImageRepository;

    public UserImageCreatedResponse createUserImages(UUID userId, List<MultipartFile> images) {
        validateInput(images);
        List<UserImageDto> userImageDtos = storeImages(userId.toString(), images);
        List<String> imageIds = saveMetaDataAsEntity(userImageDtos);
        return new UserImageCreatedResponse(userId.toString(), imageIds);
    }

    private void validateInput(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new UserImageException("No Images provided.");
        }

        if (images.size() > MAX_IMAGES_PER_USER_PROFILE) {
            throw new UserImageException("You can have only up to " + MAX_IMAGES_PER_USER_PROFILE + " images per user profile!");
        }

        for (MultipartFile image : images) {
            if (!image.getContentType().equals(MediaType.IMAGE_JPEG_VALUE) && !image.getContentType().equals(MediaType.IMAGE_PNG_VALUE)) {
                throw new UserImageException("Unsupported file type " + image.getContentType());
            }
        }
    }

    private List<UserImageDto> storeImages(String userId, List<MultipartFile> images) {
        List<UserImageDto> userImageDtoList = new ArrayList<>();
        for (MultipartFile file : images) {
            if (!file.isEmpty()) {
                String imagePath = getImageStorageStrategy().storeImage(file);
                UserImageDto userImageDto = new UserImageDto(userId, imagePath, file.getSize());
                userImageDtoList.add(userImageDto);
            }
        }
        return userImageDtoList;
    }

    private ImageStorageStrategy getImageStorageStrategy() {
        ImageStorageStrategy imageStorageStrategy = imageStorageStrategyFactory.getStrategy(ImageCategory.DATE);
        return imageStorageStrategy;
    }

    private List<String> saveMetaDataAsEntity(List<UserImageDto> userImageDtos) {
        List<String> imagesList = new ArrayList<>();
        for (UserImageDto userImage : userImageDtos) {
            UserImage image = UserImage.builder()
                    .userId(UUID.fromString(userImage.userId()))
                    .imagePath(userImage.imagePath())
                    .imageSize((int) userImage.size())
                    .build();
            UserImage savedImage = userImageRepository.save(image);
            imagesList.add(savedImage.getId().toString());
        }
        return imagesList;
    }

    public UserImageResponse getUserImages(final String userId) {
        List<UserImage> userImages = userImageRepository.findAllByUserId(UUID.fromString(userId));
        List<UserImageData> userImageDataList = loadImagesIntoDto(userImages);
        return new UserImageResponse(userImageDataList, userId);
    }

    private List<UserImageData> loadImagesIntoDto(List<UserImage> userImageList) {
        List<UserImageData> userImageDataList = new ArrayList<>();
        for (UserImage image : userImageList) {
            byte[] imageBytes = getImageStorageStrategy().loadImage(image.getImagePath());
            userImageDataList.add(new UserImageData(imageBytes, image.getId().toString()));
        }
        return userImageDataList;
    }
}
