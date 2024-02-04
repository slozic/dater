package com.slozic.dater.controllers;

import com.slozic.dater.dto.DateEventDto;
import com.slozic.dater.dto.MyDateEventDto;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.DateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/dates")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = "http://localhost:3000", originPatterns = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.GET})
public class DateEventController {

    private final DateService datesService;

    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    @GetMapping
    public List<DateEventDto> getAllDateEvents() throws UnauthorizedException {
        final UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return datesService.getDateEventDtos(UUID.randomUUID());
    }

    @GetMapping("/{id}")
    public DateEventDto getDateEventById(@PathVariable("id") final String dateId) throws UnauthorizedException {
        return datesService.getDateEventDto(dateId);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createDateEvent(@RequestParam("title") String title,
                                             @RequestParam("location") String location,
                                             @RequestParam("description") String description,
                                             @RequestParam("scheduledTime") String scheduledTime,
                                             @RequestPart("image1") MultipartFile image1) throws UnauthorizedException {
        try {
            datesService.createDateEventFromRequest(title, location, description, scheduledTime, image1);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Date event created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return an error response in case of any exception
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create date event");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/user/date")
    public List<MyDateEventDto> getDatesByCurrentUser() throws UnauthorizedException {
        final UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return datesService.getMyDateEventDtos(currentUser);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImageByDateId(@PathVariable("id") String dateId, @RequestParam("resize") boolean resize) throws IOException {
        byte[] imageBytes = datesService.getImageBytes(dateId, resize);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        // Return the image bytes along with headers
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

}
