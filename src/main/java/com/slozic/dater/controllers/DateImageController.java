package com.slozic.dater.controllers;

import com.slozic.dater.dto.response.images.DateImageCreatedResponse;
import com.slozic.dater.dto.response.images.DateImageDeletedResponse;
import com.slozic.dater.dto.response.images.DateImageResponse;
import com.slozic.dater.services.images.DateEventImageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/dates/{id}/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "DateImage", description = "DateEvent image related API calls")
@SecurityRequirement(name = "bearerAuth")
public class DateImageController {
    private final DateEventImageService dateEventImageService;

    @PostMapping
    public DateImageCreatedResponse createDateEventImages(@PathVariable("id") String dateId,
                                                          @RequestParam("files") List<MultipartFile> images) {
        return dateEventImageService.createDateEventImages(dateId, images);
    }

    @GetMapping
    public DateImageResponse getImagesByDateId(@PathVariable("id") String dateId) {
        DateImageResponse dateImageResponse = dateEventImageService.getDateEventImages(dateId);
        return dateImageResponse;
    }

    @DeleteMapping("/{imageId}")
    public DateImageDeletedResponse deleteDateEventImage(@PathVariable("id") String dateId, @PathVariable("imageId") String imageId) {

        return dateEventImageService.deleteImageFromDatabaseAndStorage(dateId, imageId);
    }

}
