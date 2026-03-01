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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Import(JwsBuilder.class)
public class DateChatControllerIT extends IntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwsBuilder jwsBuilder;

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql", "classpath:fixtures/loadChatBaseData.sql"})
    void sendDateChatMessage_shouldWorkForDateOwner() throws Exception {
        final String ownerToken = jwsBuilder.getJwt("aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5");

        final var sendResult = mockMvc.perform(
                        post("/dates/{id}/chat/messages", "be62daa9-6cda-45ea-8b0b-4ea15f735e53")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"message\":\"Hello from owner\"}"))
                .andReturn();

        assertThat(sendResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(sendResult.getResponse().getContentAsString()).contains("\"message\":\"Hello from owner\"");
        assertThat(sendResult.getResponse().getContentAsString())
                .contains("\"senderId\":\"aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5\"");
        assertThat(sendResult.getResponse().getContentAsString())
                .contains("\"recipientId\":\"6c49abd4-0e82-47f6-bb0c-558c9a890bd4\"");
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql", "classpath:fixtures/loadChatBaseData.sql"})
    void getDateChatMessages_shouldReturnMessagesForAcceptedAttendee() throws Exception {
        final String ownerToken = jwsBuilder.getJwt("aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5");
        final String acceptedToken = jwsBuilder.getJwt("6c49abd4-0e82-47f6-bb0c-558c9a890bd4");

        mockMvc.perform(
                        post("/dates/{id}/chat/messages", "be62daa9-6cda-45ea-8b0b-4ea15f735e53")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"message\":\"Hello accepted\"}"))
                .andReturn();

        final var listResult = mockMvc.perform(
                        get("/dates/{id}/chat/messages", "be62daa9-6cda-45ea-8b0b-4ea15f735e53")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + acceptedToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertThat(listResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(listResult.getResponse().getContentAsString()).contains("\"dateId\":\"be62daa9-6cda-45ea-8b0b-4ea15f735e53\"");
        assertThat(listResult.getResponse().getContentAsString()).contains("Hello accepted");
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql", "classpath:fixtures/loadChatBaseData.sql"})
    void getDateChatMessages_shouldRejectUnauthorizedUser() throws Exception {
        final String unauthorizedToken = jwsBuilder.getJwt("c041718c-2be3-4ddc-9155-7690bb123333");

        final var listResult = mockMvc.perform(
                        get("/dates/{id}/chat/messages", "be62daa9-6cda-45ea-8b0b-4ea15f735e53")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + unauthorizedToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertThat(listResult.getResponse().getStatus()).isEqualTo(403);
    }
}
