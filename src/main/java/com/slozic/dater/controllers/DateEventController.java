package com.slozic.dater.controllers;

import com.slozic.dater.dto.response.DateEventData;
import com.slozic.dater.dto.request.CreateDateEventRequest;
import com.slozic.dater.dto.response.DateEventResponse;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.DateEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/dates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "DateEvent", description = "DateEvent related API calls")
@SecurityRequirement(name = "bearerAuth")
//@CrossOrigin(origins = "http://localhost:3000", originPatterns = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.GET})
public class DateEventController {
    private final DateEventService dateEventService;
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    @GetMapping
    public DateEventResponse getAllDateEvents() throws UnauthorizedException {
        return dateEventService.getDateEvents();
    }

    @Operation(
            summary = "Retrieves a DateEvent response based on the given identifier",
            description = "Fetch a DateEvent response based on the DateEvent identifier. Response consists of the title, location and description",
            tags = "dateevents, get")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved DateEvent",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DateEventData.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id given",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "DateEvent not found!",
                    content = @Content)})
    @GetMapping("/{id}")
    public DateEventData getDateEventById(@PathVariable("id") final String dateId) throws UnauthorizedException {
        UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return dateEventService.getDateEventDto(dateId);
    }

    @PostMapping
    public ResponseEntity<?> createDateEvent(@RequestBody CreateDateEventRequest dateEventRequest) {
        UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        UUID dateId = dateEventService.createDateEvent(dateEventRequest, currentUser.toString());
        return ResponseEntity.ok(dateId);
    }

}
