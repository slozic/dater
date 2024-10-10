package com.slozic.dater.controllers;

import com.slozic.dater.controllers.params.DateQueryParameters;
import com.slozic.dater.dto.request.CreateDateEventRequest;
import com.slozic.dater.dto.response.dates.DateEventCreatedResponse;
import com.slozic.dater.dto.response.dates.DateEventListResponse;
import com.slozic.dater.dto.response.dates.DateEventResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/dates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "DateEvent", description = "DateEvent related API calls")
@SecurityRequirement(name = "bearerAuth")
public class DateEventController {
    private final DateEventService dateEventService;
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    @GetMapping
    public DateEventListResponse getAllDateEvents(final DateQueryParameters dateQueryParameters) throws UnauthorizedException {
        UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return dateEventService.getDateEvents(dateQueryParameters, currentUser);
    }

    @Operation(
            summary = "Retrieves a DateEvent response based on the given identifier",
            description = "Fetch a DateEvent response based on the DateEvent identifier. Response consists of the title, location and description",
            tags = "dateevents, get")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved DateEvent",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = DateEventResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid id given",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "DateEvent not found!",
                    content = @Content)})
    @GetMapping("/{id}")
    public DateEventResponse getDateEventById(@PathVariable("id") final String dateId) throws UnauthorizedException {
        return dateEventService.getDateEvent(dateId);
    }

    @PostMapping
    public DateEventCreatedResponse createDateEvent(@RequestBody CreateDateEventRequest dateEventRequest) {
        UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return dateEventService.createDateEventWithDefaultAttendee(dateEventRequest, currentUser.toString());
    }

    @DeleteMapping("/{id}")
    public void deleteDateEvent(@PathVariable("id") final String dateId) throws UnauthorizedException {
        dateEventService.deleteDateEvent(dateId);
    }

}
