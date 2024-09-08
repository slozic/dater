package com.slozic.dater.services;

import com.slozic.dater.dto.DateImageData;
import com.slozic.dater.dto.DateImageDto;
import com.slozic.dater.dto.DateImageResponse;
import com.slozic.dater.exceptions.DateEventException;
import com.slozic.dater.exceptions.DateImageException;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateImage;
import com.slozic.dater.repositories.DateEventRepository;
import com.slozic.dater.repositories.DateImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DateEventImageService {
    public static final int MAX_IMAGES_PER_DATE = 3;
    private final DateImageRepository dateImageRepository;
    private final DateEventRepository dateEventRepository;
    private final ImageStorageService imageStorageService;

    @Transactional
    public void createDateEventImages(final String dateId, final List<MultipartFile> images) {
        Optional<Date> optionalDate = dateEventRepository.findById(UUID.fromString(dateId));
        validateInput(dateId, images, optionalDate);

        for (MultipartFile file : images) {
            if (!file.isEmpty()) {
                String imagePath = imageStorageService.storeImage(file);
                DateImageDto dateImageDto = new DateImageDto(dateId, imagePath, file.getSize());
                saveImageAsEntity(dateImageDto);
            }
        }
    }

    private static void validateInput(String dateId, List<MultipartFile> images, Optional<Date> optionalDate) {
        if (optionalDate.isEmpty()) {
            throw new DateEventException("No DateEvents found with id: " + dateId);
        }

        if (images == null || images.isEmpty()) {
            throw new DateImageException("No Images provided for Date Event");
        }

        if (images.size() > MAX_IMAGES_PER_DATE) {
            throw new DateImageException("You can have only up to "+ MAX_IMAGES_PER_DATE +" images per date event!");
        }

        for(MultipartFile image : images){
            if(!image.getContentType().equals(MediaType.IMAGE_JPEG_VALUE) && !image.getContentType().equals(MediaType.IMAGE_PNG_VALUE)){
                throw new DateImageException("Unsupported file type " + image.getContentType());
            }
        }
    }

    private void saveImageAsEntity(final DateImageDto dateImageDto) {
        DateImage dateImage = DateImage.builder()
                .dateId(UUID.fromString(dateImageDto.dateId()))
                .imagePath(dateImageDto.imagePath())
                .imageSize((int) dateImageDto.imageSize())
                .build();
        dateImageRepository.save(dateImage);
    }

    public DateImageResponse getDateEventImages(final String dateId) {
        List<DateImage> dateImageList = dateImageRepository.findAllByDateId(UUID.fromString(dateId));
        List<DateImageData> dateImagesDataList = loadImagesIntoDto(dateImageList);
        return new DateImageResponse(dateImagesDataList, dateId);
    }

    private List<DateImageData> loadImagesIntoDto(List<DateImage> dateImageList) {
        List<DateImageData> dateImageDataList = new ArrayList<>();
        for (DateImage image : dateImageList) {
            byte[] imageBytes = imageStorageService.loadImage(image.getImagePath());
            dateImageDataList.add(new DateImageData(imageBytes, image.getId().toString()));
        }
        return dateImageDataList;
    }

    public List<DateImage> getDateEventImageMetaData(final String dateId) {
        return dateImageRepository.findAllByDateId(UUID.fromString(dateId));
    }
}
