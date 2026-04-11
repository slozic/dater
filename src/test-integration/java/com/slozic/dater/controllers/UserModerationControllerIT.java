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
public class UserModerationControllerIT extends IntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwsBuilder jwsBuilder;

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql", "classpath:fixtures/loadUsers.sql"})
    void reportUser_shouldStoreReportWithoutBlocking() throws Exception {
        final String reporterId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        final String reportedId = "6c49abd4-0e82-47f6-bb0c-558c9a890bd4";
        final String token = jwsBuilder.getJwt(reporterId);

        final var reportResult = mockMvc.perform(
                        post("/users/{id}/moderation/report", reportedId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"reason\":\"SPAM\",\"note\":\"Suspicious behavior\"}"))
                .andReturn();

        assertThat(reportResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(reportResult.getResponse().getContentAsString()).contains("\"reported\":true");
        assertThat(reportResult.getResponse().getContentAsString()).contains("\"blocked\":false");

        final var publicProfileResult = mockMvc.perform(
                        get("/users/{id}/public-profile", reportedId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andReturn();
        assertThat(publicProfileResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql", "classpath:fixtures/loadUsers.sql"})
    void blockUser_shouldForbidPublicProfileAccess() throws Exception {
        final String blockerId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        final String blockedId = "6c49abd4-0e82-47f6-bb0c-558c9a890bd4";
        final String token = jwsBuilder.getJwt(blockerId);

        final var blockResult = mockMvc.perform(
                        post("/users/{id}/moderation/block", blockedId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertThat(blockResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(blockResult.getResponse().getContentAsString()).contains("\"reported\":false");
        assertThat(blockResult.getResponse().getContentAsString()).contains("\"blocked\":true");

        final var blockedProfileResult = mockMvc.perform(
                        get("/users/{id}/public-profile", blockedId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andReturn();
        assertThat(blockedProfileResult.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql", "classpath:fixtures/loadUsers.sql", "classpath:fixtures/loadDateEvents.sql", "classpath:fixtures/loadDateAttendees.sql"})
    void reportAndBlock_shouldPreventJoinRequests() throws Exception {
        final String requesterId = "c041718c-2be3-4ddc-9155-7690bb123333";
        final String dateOwnerId = "6c49abd4-0e82-47f6-bb0c-558c9a890bd4";
        final String requesterToken = jwsBuilder.getJwt(requesterId);
        final String ownerDateId = "c7404d30-1edf-4334-97b8-b03c668b70b9";

        final var moderationResult = mockMvc.perform(
                        post("/users/{id}/moderation/report-and-block", dateOwnerId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + requesterToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"reason\":\"HARASSMENT\",\"note\":\"Do not want further contact\"}"))
                .andReturn();

        assertThat(moderationResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(moderationResult.getResponse().getContentAsString()).contains("\"reported\":true");
        assertThat(moderationResult.getResponse().getContentAsString()).contains("\"blocked\":true");

        final var joinResult = mockMvc.perform(
                        post("/dates/{id}/attendees", ownerDateId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + requesterToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertThat(joinResult.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql", "classpath:fixtures/loadChatBaseData.sql"})
    void blockUser_shouldPreventChatAccess() throws Exception {
        final String ownerId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        final String acceptedAttendeeId = "6c49abd4-0e82-47f6-bb0c-558c9a890bd4";
        final String ownerToken = jwsBuilder.getJwt(ownerId);
        final String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        mockMvc.perform(
                        post("/users/{id}/moderation/block", acceptedAttendeeId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        final var chatResult = mockMvc.perform(
                        get("/dates/{id}/chat/messages", dateId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertThat(chatResult.getResponse().getStatus()).isEqualTo(403);
    }
}
