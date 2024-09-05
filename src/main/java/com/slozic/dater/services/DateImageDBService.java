package com.slozic.dater.services;

import com.slozic.dater.models.DateImage;
import com.slozic.dater.repositories.DateImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DateImageDBService {

    private DateImageRepository dateImageRepository;

    public void saveDateEventImageToDb(String dateId, MultipartFile multipartFile, File file) {
        DateImage dateImage = DateImage.builder()
                .dateId(UUID.fromString(dateId))
                .imagePath(file.getPath())
                .imageSize((int) multipartFile.getSize())
                .build();
        dateImageRepository.save(dateImage);
    }
}
