package com.slozic.dater.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slozic.dater.models.Date;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.repositories.DateEventRepository;
import com.slozic.dater.services.DateEventService;
import com.slozic.dater.testconfig.IntegrationTest;
import com.slozic.dater.testconfig.JwsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(JwsBuilder.class)
class DateImageControllerTestIT extends IntegrationTest {

    @Autowired
    private JwsBuilder jwsBuilder;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DateEventService dateEventService;
    @Autowired
    private DateEventRepository dateEventRepository;

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql"})
    public void createDateEvent_shouldReturnSuccess() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);
        String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        var fileBytes = "image content".getBytes();
        var multipartFile = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile2 = new MockMultipartFile("files", "image2.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile3 = new MockMultipartFile("files", "image3.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);

        // when
        var mvcResult = mockMvc.perform(multipart("/dates/{dateId}/images", dateId)
                        .file(multipartFile)
                        .file(multipartFile2)
                        .file(multipartFile3)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        // then
        List<Date> dateList = dateEventRepository.findAll();
        assertThat(dateList.size()).isEqualTo(1);
        assertThat(dateList.get(0).getCreatedBy().toString()).isEqualTo(userId);
    }

}