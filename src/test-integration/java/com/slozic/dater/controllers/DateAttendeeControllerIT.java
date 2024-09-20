package com.slozic.dater.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slozic.dater.dto.response.attendees.DateAttendeeResponse;
import com.slozic.dater.exceptions.AttendeeAlreadyExistsException;
import com.slozic.dater.exceptions.AttendeeNotFoundException;
import com.slozic.dater.exceptions.DateEventException;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.DateAttendeesService;
import com.slozic.dater.testconfig.IntegrationTest;
import com.slozic.dater.testconfig.JwsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Import(JwsBuilder.class)
public class DateAttendeeControllerIT extends IntegrationTest {
    @Autowired
    private DateAttendeesService dateAttendeesService;

    @Autowired
    private JwtAuthenticatedUserService jwtAuthenticatedUserService;
    @Autowired
    private JwsBuilder jwsBuilder;
    @Autowired
    private MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql",
            "classpath:fixtures/loadDateAttendees.sql"})
    public void getDateAttendees_shouldReturnSuccess() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);
        String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        // when
        var mvcResult = mockMvc.perform(
                        get("/dates/{id}/attendees", dateId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // then
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        DateAttendeeResponse dateAttendeeResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DateAttendeeResponse.class);
        assertThat(dateAttendeeResponse.dateId()).isEqualTo(dateId);
        assertThat(dateAttendeeResponse.dateAttendees().size()).isEqualTo(2);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql",
            "classpath:fixtures/loadDateAttendees.sql"})
    public void addAttendeeToDate_shouldReturnSuccess() throws Exception {
        // given
        String userId = "c041718c-2be3-4ddc-9155-7690bb123333";
        String token = jwsBuilder.getJwt(userId);
        String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        // when
        var mvcResult = mockMvc.perform(
                        post("/dates/{id}/attendees", dateId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // then
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("{\"joinDateStatus\":\"ON_WAITLIST\",\"attendeeId\":\"c041718c-2be3-4ddc-9155-7690bb123333\",\"dateId\":\"be62daa9-6cda-45ea-8b0b-4ea15f735e53\"}");
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql",
            "classpath:fixtures/loadDateAttendees.sql"})
    public void addAttendeeToDate_shouldFailWhenAttendeeExists() throws Exception {
        // given
        String userId = "6c49abd4-0e82-47f6-bb0c-558c9a890bd4";
        String token = jwsBuilder.getJwt(userId);
        String dateId = "c7404d30-1edf-4334-97b8-b03c668b70b9";

        // when
        var mvcResult = mockMvc.perform(
                        post("/dates/{id}/attendees", dateId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // then
        assertThat(mvcResult.getResolvedException() instanceof AttendeeAlreadyExistsException).isTrue();
        assertThat(mvcResult.getResolvedException().getMessage()).isEqualTo("Attendee already requested to join date: " + dateId);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql",
            "classpath:fixtures/loadDateAttendees.sql"})
    public void addAttendeeToDate_shouldFailWhenDateDoesNotExist() throws Exception {
        // given
        String userId = "6c49abd4-0e82-47f6-bb0c-558c9a890bd4";
        String token = jwsBuilder.getJwt(userId);
        String dateId = UUID.randomUUID().toString();

        // when
        var mvcResult = mockMvc.perform(
                        post("/dates/{id}/attendees", dateId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // then
        assertThat(mvcResult.getResolvedException() instanceof DateEventException).isTrue();
        assertThat(mvcResult.getResolvedException().getMessage()).isEqualTo("Date event not found: " + dateId);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql",
            "classpath:fixtures/loadDateAttendees.sql"})
    public void acceptDateAttendee_shouldReturnSuccess() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);
        String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        // when
        var mvcResult = mockMvc.perform(
                        put("/dates/{dateId}/attendees/{userId}", dateId, userId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // then
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("{\"joinDateStatus\":\"ACCEPTED\",\"attendeeId\":\"aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5\",\"dateId\":\"be62daa9-6cda-45ea-8b0b-4ea15f735e53\"}");
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql",
            "classpath:fixtures/loadDateAttendees.sql"})
    public void acceptDateAttendee_shouldFailWhenAttendeeDoesNotExist() throws Exception {
        // given
        String userId = UUID.randomUUID().toString();
        String token = jwsBuilder.getJwt(userId);
        String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        // when
        var mvcResult = mockMvc.perform(
                        put("/dates/{dateId}/attendees/{userId}", dateId, userId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // then
        assertThat(mvcResult.getResolvedException() instanceof AttendeeNotFoundException).isTrue();
        assertThat(mvcResult.getResolvedException().getMessage()).isEqualTo("Attendee not found for date: " + dateId);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql",
            "classpath:fixtures/loadDateAttendees.sql"})
    public void getAttendeeStatus_shouldReturnSuccess() throws Exception {
        // given
        String userId = "6c49abd4-0e82-47f6-bb0c-558c9a890bd4";
        String token = jwsBuilder.getJwt(userId);
        String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        // when
        var mvcResult = mockMvc.perform(
                        get("/dates/{dateId}/attendees/status", dateId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // then
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("{\"joinDateStatus\":\"ACCEPTED\",\"attendeeId\":\"6c49abd4-0e82-47f6-bb0c-558c9a890bd4\",\"dateId\":\"be62daa9-6cda-45ea-8b0b-4ea15f735e53\"}");
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql",
            "classpath:fixtures/loadDateAttendees.sql"})
    public void getAttendeeStatus_shouldReturnNotRequested() throws Exception {
        // given
        String userId = "c041718c-2be3-4ddc-9155-7690bb123333";
        String token = jwsBuilder.getJwt(userId);
        String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        // when
        var mvcResult = mockMvc.perform(
                        get("/dates/{dateId}/attendees/status", dateId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        // then
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("{\"joinDateStatus\":\"NOT_REQUESTED\",\"attendeeId\":\"c041718c-2be3-4ddc-9155-7690bb123333\",\"dateId\":\"be62daa9-6cda-45ea-8b0b-4ea15f735e53\"}");
    }
}
