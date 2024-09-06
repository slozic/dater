package com.slozic.dater.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slozic.dater.dto.request.CreateDateEventRequest;
import com.slozic.dater.models.Date;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.repositories.DateEventRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@Import(JwsBuilder.class)
public class DateEventControllerIT extends IntegrationTest {

    @Autowired
    private JwsBuilder jwsBuilder;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DateEventService dateEventService;
    @Autowired
    private DateEventRepository dateEventRepository;

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
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("[{\"id\":\"be62daa9-6cda-45ea-8b0b-4ea15f735e53\",\"title\":\"Date in the Alps\",\"location\":\"Alps\",\"description\":\"Perfect afternoon date in the alps\",\"dateOwner\":\"clara\",\"dateJoiner\":\"\",\"scheduledTime\":\"2024-01-29T20:00Z\",\"joinStatus\":\"\"},{\"id\":\"c7404d30-1edf-4334-97b8-b03c668b70b9\",\"title\":\"Morning run by the lake\",\"location\":\"Lake\",\"description\":\"Lets have a morning run by the lake and then nice smoothie bowl for breakfast!\",\"dateOwner\":\"tom.h\",\"dateJoiner\":\"\",\"scheduledTime\":\"2024-02-02T22:00Z\",\"joinStatus\":\"\"}]");
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
        /*var fileBytes = "image content".getBytes();
        var multipartFile = new MockMultipartFile("image1", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);*/

        final CreateDateEventRequest createDateEventRequest = getCreateDateEventRequest();

        // when
        var mvcResult = mockMvc.perform(post("/dates")
                        .content(objectMapper.writeValueAsString(createDateEventRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andReturn();

        // then
        List<Date> dateList = dateEventRepository.findAll();
        assertThat(dateList.size()).isEqualTo(1);
        assertThat(dateList.get(0).getCreatedBy().toString()).isEqualTo(userId);
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
        List<Date> dateList = dateEventRepository.findAll();
        assertThat(dateList.size()).isEqualTo(0);
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
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
