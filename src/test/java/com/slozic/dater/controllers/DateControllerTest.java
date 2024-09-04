package com.slozic.dater.controllers;

import com.slozic.dater.security.JwtAuthenticatedUserService;
import com.slozic.dater.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class DateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DateAttendeesService dateAttendeesService;

    @MockBean
    private DateEventService dateEventService;

    @MockBean
    private MyDateEventService myDateEventService;

    @MockBean
    private DateImageService dateImageService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticatedUserService jwtAuthenticatedUserService;

    @Test
    public void getAllDates_WorksWithSuccess() throws Exception {
        SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwt = jwt()
                .authorities(new SimpleGrantedAuthority("SCOPE_dummy:scope"));
        // when
        var mvcResult = mockMvc.perform(get("/dates").with(jwt)).andReturn();

        // then
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    public void getDateById_WorksWithSuccess() throws Exception {
        SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwt = jwt()
                .authorities(new SimpleGrantedAuthority("SCOPE_dummy:scope"));
        // when
        var mvcResult = mockMvc.perform(get("/dates").param("id", "1").with(jwt)).andReturn();

        // then
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void getDateById_WhenUnauthenticatedThenUnauthorized() throws Exception {
        this.mockMvc.perform(get("/dates"))
                .andExpect(status().isUnauthorized());
    }

}
