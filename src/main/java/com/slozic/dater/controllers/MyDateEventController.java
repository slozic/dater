package com.slozic.dater.controllers;

import com.slozic.dater.dto.MyDateEventDto;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.MyDateEventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dates/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "MyDateEvent", description = "My DateEvent related API calls")
@SecurityRequirement(name = "bearerAuth")
public class MyDateEventController {
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;
    private final MyDateEventService myDateEventService;

    @GetMapping
    public List<MyDateEventDto> getDatesByCurrentlyLoggedUser() throws UnauthorizedException {
        final UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        return myDateEventService.getMyDateEventDtos(currentUser);
    }
}
