package com.slozic.dater.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slozic.dater.dto.CreateDateEventRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest
public class GetDatesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAllDatesWorksWithSuccess() throws Exception {
        // when
        var mvcResult = mockMvc.perform(get("/dates")).andReturn();

        // then
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    public void getDateByIdWorksWithSuccess() throws Exception {
        // when
        var mvcResult = mockMvc.perform(get("/dates").param("id", "1")).andReturn();

        // then
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    public void createDateEventWorksWithSuccess() throws Exception {
        // given
        final CreateDateEventRequest createDateEventRequest = new CreateDateEventRequest("id", "location", "description");
        ObjectMapper objectMapper = new ObjectMapper();
        final String requestAsString = objectMapper.writeValueAsString(createDateEventRequest);
        // when
        var mvcResult = mockMvc.perform(post("/dates").content(requestAsString).contentType(MediaType.APPLICATION_JSON)).andReturn();

        // then
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }
}
