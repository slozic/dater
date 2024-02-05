package com.slozic.dater.controllers;

import com.slozic.dater.repositories.DateRepository;
import com.slozic.dater.services.DateService;
import com.slozic.dater.testconfig.IntegrationTest;
import com.slozic.dater.testconfig.JwsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Import(JwsBuilder.class)
public class DateEventControllerIT extends IntegrationTest {

    @Autowired
    private JwsBuilder jwsBuilder;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DateService dateService;

    @Autowired
    private DateRepository dateRepository;

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
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(403);
    }
}
