package com.slozic.dater.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slozic.dater.dto.request.CreateDateEventRequest;
import com.slozic.dater.dto.response.dates.DateEventCreatedResponse;
import com.slozic.dater.dto.response.dates.DateEventListResponse;
import com.slozic.dater.dto.response.images.DateImageMetaData;
import com.slozic.dater.exceptions.DateEventAccessPermissionException;
import com.slozic.dater.exceptions.DateEventNotFoundException;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.services.DateEventImageService;
import com.slozic.dater.services.DateEventService;
import com.slozic.dater.testconfig.IntegrationTest;
import com.slozic.dater.testconfig.JwsBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(JwsBuilder.class)
public class DateEventControllerIT extends IntegrationTest {
    @Autowired
    private JwsBuilder jwsBuilder;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DateEventService dateEventService;
    @Autowired
    private DateEventImageService dateEventImageService;
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
        DateEventListResponse dateEventListResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DateEventListResponse.class);
        assertThat(dateEventListResponse.dateEventData().size()).isEqualTo(2);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql"})
    public void getAllDateEvents_shouldLoadUserOwnedDatesWithSuccess() throws Exception {
        // given
        String token = jwsBuilder.getJwt("aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5");

        // when
        var mvcResult = mockMvc.perform(
                        get("/dates").param("filter", "owned")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        DateEventListResponse dateEventListResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DateEventListResponse.class);
        assertThat(dateEventListResponse.dateEventData().size()).isEqualTo(1);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql"})
    public void getAllDateEvents_shouldLoadUserRequestedDatesWithSuccess() throws Exception {
        // given
        String token = jwsBuilder.getJwt("aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5");

        // when
        var mvcResult = mockMvc.perform(
                        get("/dates").param("filter", "owned")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        DateEventListResponse dateEventListResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DateEventListResponse.class);
        assertThat(dateEventListResponse.dateEventData().size()).isEqualTo(1);
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
                .andExpect(status().isOk())
                .andReturn();

        // then
        DateEventCreatedResponse createdResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DateEventCreatedResponse.class);
        assertThat(createdResponse.dateEventId()).isNotBlank();
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
        assertThat(mvcResult.getResolvedException().getClass()).isEqualTo(DateEventNotFoundException.class);
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

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql",
            "classpath:fixtures/loadDateAttendees.sql",
            "classpath:fixtures/loadDateImages.sql"})
    public void deleteDateEvent_shouldReturnSuccess() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);
        String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        List<DateImageMetaData> imageMetaDataList = dateEventImageService.getDateEventImageMetaData(dateId);

        // when
        var mvcResult = mockMvc.perform(delete("/dates/{id}", dateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        // then
        Assertions.assertThrows(DateEventNotFoundException.class, () -> dateEventService.getDateEvent(dateId));
        assertThat(dateAttendeeRepository.findAllByIdDateId(UUID.fromString(dateId))).isEmpty();
        assertThat(dateEventImageService.getDateEventImageMetaData(dateId)).isEmpty();
        imageMetaDataList.forEach(dateImageMetaData -> assertThat(new File(dateImageMetaData.path()).exists()).isFalse());
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql",
            "classpath:fixtures/loadDateAttendees.sql"})
    public void deleteDateEvent_shouldFailWhenUserIsNotDateOwner() throws Exception {
        // given
        String userId = "6c49abd4-0e82-47f6-bb0c-558c9a890bd4";
        String token = jwsBuilder.getJwt(userId);
        String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        // when
        var mvcResult = mockMvc.perform(delete("/dates/{id}", dateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(result -> assertThat(result.getResolvedException().getClass()).isEqualTo(DateEventAccessPermissionException.class))
                .andReturn();
        // then
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql",
            "classpath:fixtures/loadDateAttendees.sql"})
    public void deleteDateEvent_shouldFailWhenDateDoesNotExist() throws Exception {
        // given
        String userId = "6c49abd4-0e82-47f6-bb0c-558c9a890bd4";
        String token = jwsBuilder.getJwt(userId);
        String dateId = UUID.randomUUID().toString();

        // when
        var mvcResult = mockMvc.perform(delete("/dates/{id}", dateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(result -> assertThat(result.getResolvedException().getClass()).isEqualTo(DateEventNotFoundException.class))
                .andReturn();
        // then
    }

}
