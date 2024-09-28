package com.slozic.dater.services;

import com.slozic.dater.dto.ProfileImageDto;
import com.slozic.dater.dto.enums.ImageCategory;
import com.slozic.dater.dto.response.userprofile.ProfileImageCreatedResponse;
import com.slozic.dater.dto.response.userprofile.ProfileImageData;
import com.slozic.dater.dto.response.userprofile.ProfileImageResponse;
import com.slozic.dater.exceptions.UserImageException;
import com.slozic.dater.models.UserImage;
import com.slozic.dater.repositories.ProfileImageRepository;
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
public class ProfileImageService {
    @Value("${user.images.max-count}")
    private int MAX_IMAGES_PER_USER_PROFILE;
    @Autowired
    private ImageStorageStrategyFactory imageStorageStrategyFactory;
    @Autowired
    private ProfileImageRepository profileImageRepository;

    public ProfileImageCreatedResponse createProfileImages(UUID userId, List<MultipartFile> images) {
        validateInput(images);
        List<ProfileImageDto> profileImageDtos = storeImages(userId.toString(), images);
        List<String> imageIds = saveMetaDataAsEntity(profileImageDtos);
        return new ProfileImageCreatedResponse(userId.toString(), imageIds);
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

    private List<ProfileImageDto> storeImages(String userId, List<MultipartFile> images) {
        List<ProfileImageDto> profileImageDtoList = new ArrayList<>();
        for (MultipartFile file : images) {
            if (!file.isEmpty()) {
                String imagePath = getImageStorageStrategy().storeImage(file);
                ProfileImageDto profileImageDto = new ProfileImageDto(userId, imagePath, file.getSize());
                profileImageDtoList.add(profileImageDto);
            }
        }
        return profileImageDtoList;
    }

    private ImageStorageStrategy getImageStorageStrategy() {
        ImageStorageStrategy imageStorageStrategy = imageStorageStrategyFactory.getStrategy(ImageCategory.DATE);
        return imageStorageStrategy;
    }

    private List<String> saveMetaDataAsEntity(List<ProfileImageDto> profileImageDtos) {
        List<String> imagesList = new ArrayList<>();
        for (ProfileImageDto userImage : profileImageDtos) {
            UserImage image = UserImage.builder()
                    .userId(UUID.fromString(userImage.userId()))
                    .imagePath(userImage.imagePath())
                    .imageSize((int) userImage.size())
                    .build();
            UserImage savedImage = profileImageRepository.save(image);
            imagesList.add(savedImage.getId().toString());
        }
        return imagesList;
    }

    public ProfileImageResponse getProfileImages(final String userId) {
        List<UserImage> userImages = profileImageRepository.findAllByUserId(UUID.fromString(userId));
        List<ProfileImageData> profileImageDataList = loadImagesIntoDto(userImages);
        return new ProfileImageResponse(profileImageDataList, userId);
    }

    private List<ProfileImageData> loadImagesIntoDto(List<UserImage> userImageList) {
        List<ProfileImageData> profileImageDataList = new ArrayList<>();
        for (UserImage image : userImageList) {
            byte[] imageBytes = getImageStorageStrategy().loadImage(image.getImagePath());
            profileImageDataList.add(new ProfileImageData(imageBytes, image.getId().toString()));
        }
        return profileImageDataList;
    }
}
