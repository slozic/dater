package com.slozic.dater.controllers;

import com.slozic.dater.dto.DateAttendeeDto;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("[{\"id\":\"aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5\",\"username\":\"clara\",\"accepted\":true},{\"id\":\"6c49abd4-0e82-47f6-bb0c-558c9a890bd4\",\"username\":\"tom.h\",\"accepted\":true}]");
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql",
            "classpath:fixtures/loadDateAttendees.sql"})
    public void addDateAttendee_shouldReturnSuccess() throws Exception {
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
        List<DateAttendeeDto> dateAttendeeDtoList = dateAttendeesService.getDateAttendeeDtos(dateId);
        assertThat(dateAttendeeDtoList).flatMap(DateAttendeeDto::id).contains(userId);
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("User added successfully");
    }
}
