package com.slozic.dater.controllers;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@Import(JwsBuilder.class)
class UsersControllerIT extends IntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwsBuilder jwsBuilder;

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql", "classpath:fixtures/loadUsers.sql"})
    void updateProfile_shouldUpdateNotificationPreferences() throws Exception {
        final String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        final String token = jwsBuilder.getJwt(userId);

        final var updateResult = mockMvc.perform(
                        put("/users/profile")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "attendeeAcceptedNotificationsEnabled": false,
                                          "dateRequestNotificationsEnabled": false,
                                          "chatMessageNotificationsEnabled": true
                                        }
                                        """))
                .andReturn();

        final var getResult = mockMvc.perform(
                        get("/users")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertThat(updateResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(updateResult.getResponse().getContentAsString())
                .contains("\"attendeeAcceptedNotificationsEnabled\":false");
        assertThat(updateResult.getResponse().getContentAsString())
                .contains("\"dateRequestNotificationsEnabled\":false");
        assertThat(updateResult.getResponse().getContentAsString())
                .contains("\"chatMessageNotificationsEnabled\":true");

        assertThat(getResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(getResult.getResponse().getContentAsString())
                .contains("\"attendeeAcceptedNotificationsEnabled\":false");
        assertThat(getResult.getResponse().getContentAsString())
                .contains("\"dateRequestNotificationsEnabled\":false");
        assertThat(getResult.getResponse().getContentAsString())
                .contains("\"chatMessageNotificationsEnabled\":true");
    }
}
