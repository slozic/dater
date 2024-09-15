package com.slozic.dater.services;

import com.slozic.dater.dto.request.CreateDateEventRequest;
import com.slozic.dater.dto.response.dates.DateEventCreatedResponse;
import com.slozic.dater.exceptions.DateEventException;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.repositories.DateEventRepository;
import com.slozic.dater.repositories.DateImageRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DateEventServiceTest {
    @Mock
    private DateEventRepository dateEventRepository;
    @Mock
    private DateAttendeeRepository dateAttendeeRepository;
    @Mock
    private DateImageRepository dateImageRepository;
    @Mock
    private JwtAuthenticatedUserService jwtAuthenticatedUserService;
    @Mock
    private LocalImageStorageService dateImageService;
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
        MockMultipartFile imageFile = new MockMultipartFile("image1", "test.jpg", "image/jpeg", "some image".getBytes());

        // when
        UUID currentUser = UUID.randomUUID();

        when(dateEventRepository.save(Mockito.any(Date.class))).thenAnswer(invocation -> {
            Date date = invocation.getArgument(0);
            date.setId(currentUser); // Simulate database behavior
            return date;
        });

        when(dateAttendeesService.createDefaultDateAttendee(Mockito.any(Date.class))).thenReturn(new DateAttendee());

        // then
        CreateDateEventRequest request = new CreateDateEventRequest(title, location, description, scheduledTime);
        DateEventCreatedResponse result = dateEventService.createDateEventWithDefaultAttendee(request, currentUser.toString());
        assertNotNull(result);
    }
}
