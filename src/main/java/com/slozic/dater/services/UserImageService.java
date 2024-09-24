package com.slozic.dater.services;

import com.slozic.dater.dto.UserImageCreatedResponse;
import com.slozic.dater.dto.response.userprofile.UserImageDto;
import com.slozic.dater.exceptions.DateImageException;
import com.slozic.dater.models.UserImage;
import com.slozic.dater.repositories.UserImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserImageService {
    private static final int MAX_IMAGES_PER_USER_PROFILE = 3;
    private final ImageStorageService imageStorageService;
    private final UserImageRepository userImageRepository;

    public UserImageCreatedResponse createUserImages(UUID userId, List<MultipartFile> images) {
        validateInput(images);
        List<UserImageDto> userImageDtos = storeImages(userId.toString(), images);
        List<String> imageIds = saveMetaDataAsEntity(userImageDtos);
        return new UserImageCreatedResponse(userId.toString(), imageIds);
    }

    private void validateInput(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new DateImageException("No Images provided for Date Event");
        }

        if (images.size() > MAX_IMAGES_PER_USER_PROFILE) {
            throw new DateImageException("You can have only up to " + MAX_IMAGES_PER_USER_PROFILE + " images per user profile!");
        }

        for (MultipartFile image : images) {
            if (!image.getContentType().equals(MediaType.IMAGE_JPEG_VALUE) && !image.getContentType().equals(MediaType.IMAGE_PNG_VALUE)) {
                throw new DateImageException("Unsupported file type " + image.getContentType());
            }
        }
    }

    private List<UserImageDto> storeImages(String userId, List<MultipartFile> images) {
        List<UserImageDto> userImageDtoList = new ArrayList<>();
        for (MultipartFile file : images) {
            if (!file.isEmpty()) {
                String imagePath = imageStorageService.storeImage(file);
                UserImageDto userImageDto = new UserImageDto(userId, imagePath, file.getSize());
                userImageDtoList.add(userImageDto);
            }
        }
        return userImageDtoList;
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

}
