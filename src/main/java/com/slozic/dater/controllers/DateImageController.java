package com.slozic.dater.controllers;

import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.DateEventImageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dates/{id}/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "DateImage", description = "DateEvent image related API calls")
@SecurityRequirement(name = "bearerAuth")
public class DateImageController {
    private final DateEventImageService dateEventImageService;
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    @PostMapping
    public ResponseEntity<?> uploadDateImages(@PathVariable("id") String dateId,
                                              @RequestParam("files") List<MultipartFile> images) {
        UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        dateEventImageService.addImagesToDateEvent(dateId, images);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<byte[]>> getImageByDateId(@PathVariable("id") String dateId) {
        List<byte[]> imageBytes = dateEventImageService.getDateEventImages(dateId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
}
