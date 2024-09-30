package com.slozic.dater.services;

import com.slozic.dater.dto.DateImageDto;
import com.slozic.dater.dto.enums.ImageCategory;
import com.slozic.dater.dto.response.images.DateImageCreatedResponse;
import com.slozic.dater.dto.response.images.DateImageData;
import com.slozic.dater.dto.response.images.DateImageResponse;
import com.slozic.dater.exceptions.DateEventException;
import com.slozic.dater.models.DateImage;
import com.slozic.dater.repositories.DateEventRepository;
import com.slozic.dater.repositories.DateImageRepository;
import com.slozic.dater.services.images.ImageStorageStrategy;
import com.slozic.dater.services.images.ImageStorageStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class DateEventImageService {
    @Autowired
    private DateImageRepository dateImageRepository;
    @Autowired
    private DateEventRepository dateEventRepository;
    @Autowired
    private ImageStorageStrategyFactory imageStorageStrategyFactory;

    @Transactional
    public DateImageCreatedResponse createDateEventImages(final String dateId, final List<MultipartFile> images) {
        dateEventRepository.findById(UUID.fromString(dateId)).orElseThrow(() ->
                new DateEventException("No DateEvents found with id: " + dateId));

        getImageStorageStrategy().validate(images);
        List<DateImageDto> dateImageDtos = storeImages(dateId, images);
        List<String> imageIds = saveMetaDataAsEntity(dateImageDtos);
        return new DateImageCreatedResponse(dateId, imageIds);
    }

    private List<DateImageDto> storeImages(String dateId, List<MultipartFile> images) {
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

    private List<DateImageData> loadImagesIntoDto(List<DateImage> dateImageList) {
        List<DateImageData> dateImageDataList = new ArrayList<>();
        for (DateImage image : dateImageList) {
            byte[] imageBytes = getImageStorageStrategy().loadImage(image.getImagePath());
            dateImageDataList.add(new DateImageData(imageBytes, image.getId().toString()));
        }
        return dateImageDataList;
    }

    public List<DateImage> getDateEventImageMetaData(final String dateId) {
        return dateImageRepository.findAllByDateId(UUID.fromString(dateId));
    }
}
