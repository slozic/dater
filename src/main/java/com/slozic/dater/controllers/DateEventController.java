package com.slozic.dater.controllers;

import com.slozic.dater.dto.DateEventDto;
import com.slozic.dater.dto.MyDateEventDto;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.DateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/dates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "DateEvent", description = "DateEvent related API calls")
@SecurityRequirement(name = "bearerAuth")
//@CrossOrigin(origins = "http://localhost:3000", originPatterns = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.GET})
public class DateEventController {

    private final DateService datesService;

    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    @GetMapping
    public List<DateEventDto> getAllDateEvents() throws UnauthorizedException {
        return datesService.getDateEventDtos();
    }

    @Operation(
            summary = "Retrieves a DateEvent response based on the given identifier",
            description = "Fetch a DateEvent response based on the DateEvent identifier. Response consists of the title, location and description",
            tags = "dateevents, get")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved DateEvent",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DateEventDto.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id given",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "DateEvent not found!",
                    content = @Content)})
    @GetMapping("/{id}")
    public DateEventDto getDateEventById(@PathVariable("id") final String dateId) throws UnauthorizedException {
        return datesService.getDateEventDto(dateId);
    }

    @PostMapping
    public ResponseEntity<?> createDateEvent(@RequestParam("title") String title,
                                             @RequestParam("location") String location,
                                             @RequestParam("description") String description,
                                             @RequestParam("scheduledTime") String scheduledTime,
                                             @RequestPart("image1") Optional<MultipartFile> image1) {
        datesService.createDateEventFromRequest(title, location, description, scheduledTime, image1);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Date event created successfully");
        return ResponseEntity.ok(response);
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
