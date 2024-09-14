package com.slozic.dater.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slozic.dater.dto.request.CreateDateEventRequest;
import com.slozic.dater.dto.response.DateEventResponse;
import com.slozic.dater.exceptions.DateEventException;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.services.DateEventService;
import com.slozic.dater.testconfig.IntegrationTest;
import com.slozic.dater.testconfig.JwsBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Import(JwsBuilder.class)
public class DateEventControllerIT extends IntegrationTest {
    @Autowired
    private JwsBuilder jwsBuilder;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DateEventService dateEventService;

    @Autowired
    private DateAttendeeRepository dateAttendeeRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql"})
    public void getAllDateEvents_shouldLoadWithSuccess() throws Exception {
        // given
        String token = jwsBuilder.getJwt("aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5");

        // when
        var mvcResult = mockMvc.perform(
                        get("/dates")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        DateEventResponse dateEventResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DateEventResponse.class);
        assertThat(dateEventResponse.dateEventData().size()).isEqualTo(2);
    }

    @Test
    public void getAllDateEvents_shouldBlockUnauthorisedAccess() throws Exception {
        // when
        var mvcResult = mockMvc.perform(
                        get("/dates")
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql"})
    public void createDateEvent_shouldReturnSuccess() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);

        final CreateDateEventRequest createDateEventRequest = getCreateDateEventRequest();

        // when
        var mvcResult = mockMvc.perform(post("/dates")
                        .content(objectMapper.writeValueAsString(createDateEventRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andReturn();

        // then
        DateEventResponse dateEventResponse = dateEventService.getDateEvents();
        assertThat(dateEventResponse.dateEventData().size()).isEqualTo(1);
        assertThat(dateEventResponse.dateEventData().get(0).dateOwnerId()).isEqualTo(userId);
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql"})
    public void createDateEvent_shouldFailWithNonExistingUser() throws Exception {
        // given
        // random non-existing user
        String userId = "";
        String token = jwsBuilder.getJwt(userId);

        final CreateDateEventRequest createDateEventRequest = getCreateDateEventRequest();

        // when
        var mvcResult = mockMvc.perform(post("/dates")
                        .content(objectMapper.writeValueAsString(createDateEventRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andReturn();

        // then
        DateEventResponse dateEventResponse = dateEventService.getDateEvents();
        assertThat(dateEventResponse.dateEventData().size()).isEqualTo(0);
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql"})
    public void getDateEvent_shouldFailWithNonExistingDateid() throws Exception {
        // given
        String token = jwsBuilder.getJwt("aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5");
        String dateId = UUID.randomUUID().toString();

        // when
        var mvcResult = mockMvc.perform(
                        get("/dates/{id}", dateId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        assertThat(mvcResult.getResolvedException().getClass()).isEqualTo(DateEventException.class);
    }

    @NotNull
    private static CreateDateEventRequest getCreateDateEventRequest() {
        final CreateDateEventRequest createDateEventRequest = new CreateDateEventRequest(
                "title",
                "description",
                "location",
                "2024-01-29T20:00");
        return createDateEventRequest;
    }
}
