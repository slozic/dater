package com.slozic.dater.services;

import com.slozic.dater.dto.DateImageDto;
import com.slozic.dater.dto.enums.ImageCategory;
import com.slozic.dater.dto.response.images.*;
import com.slozic.dater.exceptions.DateEventException;
import com.slozic.dater.exceptions.DateEventNotFoundException;
import com.slozic.dater.exceptions.DateImageAccessException;
import com.slozic.dater.exceptions.DateImageNotFoundException;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateImage;
import com.slozic.dater.repositories.DateEventRepository;
import com.slozic.dater.repositories.DateImageRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.images.ImageStorageStrategy;
import com.slozic.dater.services.images.ImageStorageStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class DateEventImageService {
    private final DateImageRepository dateImageRepository;
    private final DateEventRepository dateEventRepository;
    private final ImageStorageStrategyFactory imageStorageStrategyFactory;
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    @Transactional
    public DateImageCreatedResponse createDateEventImages(final String dateId, final List<MultipartFile> images) {
        dateEventRepository.findById(UUID.fromString(dateId)).orElseThrow(() ->
                new DateEventException("No DateEvents found with id: " + dateId));

        getImageStorageStrategy().validate(images);
        List<DateImageDto> dateImageDtos = storeImages(dateId, images);
        List<String> imageIds = saveMetaDataAsEntity(dateImageDtos);
        return new DateImageCreatedResponse(dateId, imageIds);
    }

    private List<DateImageDto> storeImages(final String dateId, final List<MultipartFile> images) {
        List<DateImageDto> dateImageDtoList = new ArrayList<>();
        for (MultipartFile file : images) {
            if (!file.isEmpty()) {
                String imagePath = getImageStorageStrategy().storeImage(file);
                DateImageDto dateImageDto = new DateImageDto(dateId, imagePath, file.getSize());
                dateImageDtoList.add(dateImageDto);
            }
        }
        return dateImageDtoList;
    }

    private ImageStorageStrategy getImageStorageStrategy() {
        ImageStorageStrategy imageStorageStrategy = imageStorageStrategyFactory.getStrategy(ImageCategory.DATE);
        return imageStorageStrategy;
    }

    private List<String> saveMetaDataAsEntity(final List<DateImageDto> dateImageDtos) {
        List<String> imagesList = new ArrayList<>();
        for (DateImageDto dateImage : dateImageDtos) {
            DateImage image = DateImage.builder()
                    .dateId(UUID.fromString(dateImage.dateId()))
                    .imagePath(dateImage.imagePath())
                    .imageSize((int) dateImage.imageSize())
                    .build();
            DateImage savedImage = dateImageRepository.save(image);
            imagesList.add(savedImage.getId().toString());
        }
        return imagesList;
    }

    public DateImageResponse getDateEventImages(final String dateId) {
        List<DateImage> dateImageList = dateImageRepository.findAllByDateId(UUID.fromString(dateId));
        List<DateImageData> dateImagesDataList = loadImagesIntoDto(dateImageList);
        return new DateImageResponse(dateImagesDataList, dateId);
    }

    private List<DateImageData> loadImagesIntoDto(final List<DateImage> dateImageList) {
        List<DateImageData> dateImageDataList = new ArrayList<>();
        for (DateImage image : dateImageList) {
            byte[] imageBytes = getImageStorageStrategy().loadImage(image.getImagePath());
            dateImageDataList.add(new DateImageData(imageBytes, image.getId().toString()));
        }
        return dateImageDataList;
    }

    public List<DateImageMetaData> getDateEventImageMetaData(final String dateId) {
        List<DateImage> dateImageList = dateImageRepository.findAllByDateId(UUID.fromString(dateId));
        return getDateImageMetaDataList(dateImageList);
    }

    private List<DateImageMetaData> getDateImageMetaDataList(final List<DateImage> dateImageList) {
        return dateImageList.stream()
                .map(dateImage -> new DateImageMetaData(dateImage.getId().toString(), dateImage.getImagePath(), dateImage.getDateId().toString(), dateImage.getImageSize()))
                .collect(Collectors.toList());
    }

    @Transactional
    public DateImageDeletedResponse deleteImageFromDatabaseAndStorage(final String dateId, final String imageId) {
        validateUserDateEventPermissions(dateId);
        DateImage dateImage = getImageEntity(imageId);
        getImageStorageStrategy().deleteImage(dateImage.getImagePath());
        dateImageRepository.delete(dateImage);
        return new DateImageDeletedResponse(dateId, imageId);
    }

    private void validateUserDateEventPermissions(String dateId) {
        UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        Optional<Date> optionalDate = dateEventRepository.findById(UUID.fromString(dateId));
        if (optionalDate.isEmpty()) {
            throw new DateEventNotFoundException("Date event not found: " + dateId);
        }

        if (!optionalDate.get().getCreatedBy().equals(currentUser)) {
            throw new DateImageAccessException("User does have permission to delete the date image");
        }
    }

    private DateImage getImageEntity(String imageId) {
        Optional<DateImage> optionalDateImage = dateImageRepository.findById(UUID.fromString(imageId));

        if (optionalDateImage.isEmpty()) {
            throw new DateImageNotFoundException("Date image with id was not found: " + imageId);
        }
        return optionalDateImage.get();
    }

    @Transactional
    public void deleteAllImages(final Date dateEvent) {
        List<DateImage> dateImageList = dateImageRepository.findAllByDateId(dateEvent.getId());
        deleteImagesFromDatabaseAndStorage(dateEvent.getId().toString(), dateImageList);
    }

    private DateImageDeletedResponseList deleteImagesFromDatabaseAndStorage(final String dateId, final List<DateImage> dateImageList) {
        dateImageList.forEach(dateImage -> getImageStorageStrategy()
                .deleteImage(dateImage.getImagePath()));
        dateImageRepository.deleteAllInBatch(dateImageList);

        return new DateImageDeletedResponseList(dateId, dateImageList.stream()
                .map(dateImage -> dateImage.getId().toString())
                .collect(Collectors.toList()));
    }
}
