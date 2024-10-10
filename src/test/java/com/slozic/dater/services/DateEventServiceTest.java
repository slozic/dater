package com.slozic.dater.services;

import com.slozic.dater.dto.request.CreateDateEventRequest;
import com.slozic.dater.dto.response.dates.DateEventCreatedResponse;
import com.slozic.dater.exceptions.dateevent.DateEventException;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.repositories.DateEventRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.attendees.DateAttendeesService;
import com.slozic.dater.services.dateevent.DateEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DateEventServiceTest {
    @Mock
    private DateEventRepository dateEventRepository;
    @Mock
    private JwtAuthenticatedUserService jwtAuthenticatedUserService;
    @Mock
    private DateAttendeesService dateAttendeesService;
    @InjectMocks
    private DateEventService dateEventService;

    @Test
    public void createDateEventFromRequest_Success() throws UnauthorizedException, DateEventException {
        // given
        String title = "Test Event";
        String location = "Test Location";
        String description = "Test Description";
        String scheduledTime = LocalDateTime.now().plusDays(1).toString();
        UUID currentUser = UUID.randomUUID();

        when(jwtAuthenticatedUserService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(dateAttendeesService.createDefaultDateAttendee(Mockito.any(Date.class))).thenReturn(new DateAttendee());
        when(dateEventRepository.save(Mockito.any(Date.class))).thenAnswer(invocation -> {
            Date date = invocation.getArgument(0);
            date.setId(UUID.randomUUID()); // Simulate database behavior
            return date;
        });

        // when
        CreateDateEventRequest request = new CreateDateEventRequest(title, location, description, scheduledTime);
        DateEventCreatedResponse result = dateEventService.createDateEventWithDefaultAttendee(request);

        // then
        assertNotNull(result);
        assertThat(result.dateEventId()).isNotBlank();
    }
}
