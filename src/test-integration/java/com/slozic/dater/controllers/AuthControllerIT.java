package com.slozic.dater.controllers;

import com.slozic.dater.testconfig.IntegrationTest;
import com.slozic.dater.testconfig.JwsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Import(JwsBuilder.class)
public class AuthControllerIT extends IntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwsBuilder jwsBuilder;

    @Test
    void refreshToken_shouldReturnRotatedTokens() throws Exception {
        final String refreshToken = jwsBuilder.getRefreshJwt("aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5");

        final var mvcResult = mockMvc.perform(
                        post("/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"refreshToken\":\"Bearer " + refreshToken + "\"}"))
                .andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(mvcResult.getResponse().getContentAsString()).contains("\"accessToken\":\"Bearer ");
        assertThat(mvcResult.getResponse().getContentAsString()).contains("\"refreshToken\":\"Bearer ");
    }

    @Test
    void refreshToken_shouldRejectAccessToken() throws Exception {
        final String accessToken = jwsBuilder.getJwt("aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5");

        final var mvcResult = mockMvc.perform(
                        post("/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"refreshToken\":\"Bearer " + accessToken + "\"}"))
                .andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(401);
    }
}
