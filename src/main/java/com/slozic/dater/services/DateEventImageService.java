package com.slozic.dater.services;

import com.slozic.dater.dto.DateImageDto;
import com.slozic.dater.exceptions.DateEventException;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateImage;
import com.slozic.dater.repositories.DateEventRepository;
import com.slozic.dater.repositories.DateImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final DateImageRepository dateImageRepository;
    private final DateEventRepository dateEventRepository;
    private final ImageStorageService imageStorageService;

    @Transactional
    public void addImagesToDateEvent(final String dateId, final List<MultipartFile> images) {
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
            throw new IllegalArgumentException("No Images provided for upload.");
        }
    }

    public void saveImageAsEntity(final DateImageDto dateImageDto) {
        DateImage dateImage = DateImage.builder()
                .dateId(UUID.fromString(dateImageDto.dateId()))
                .imagePath(dateImageDto.imagePath())
                .imageSize((int) dateImageDto.imageSize())
                .build();
        dateImageRepository.save(dateImage);
    }

    public List<byte[]> getDateEventImages(final String dateId) {
        List<byte[]> byteImageList = new ArrayList<>();
        List<DateImage> dateImageList = dateImageRepository.findAllByDateId(UUID.fromString(dateId));

        for (DateImage image : dateImageList) {
            byteImageList.add(imageStorageService.loadImage(image.getImagePath()));
        }
        return byteImageList;
    }
}
