package com.slozic.dater.controllers;

import com.slozic.dater.dto.DateImageResponse;
import com.slozic.dater.services.DateEventImageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> createDateEventImages(@PathVariable("id") String dateId,
                                              @RequestParam("files") List<MultipartFile> images) {
        dateEventImageService.createDateEventImages(dateId, images);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<DateImageResponse> getImagesByDateId(@PathVariable("id") String dateId) {
        DateImageResponse dateImageResponse = dateEventImageService.getDateEventImages(dateId);
        return ResponseEntity.ok(dateImageResponse);
    }
}
