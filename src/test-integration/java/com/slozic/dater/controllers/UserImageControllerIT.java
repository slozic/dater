package com.slozic.dater.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slozic.dater.dto.response.userprofile.UserImageCreatedResponse;
import com.slozic.dater.dto.response.userprofile.UserImageResponse;
import com.slozic.dater.services.UserImageService;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(JwsBuilder.class)
class UserImageControllerIT extends IntegrationTest {

    private static final String RESOURCES_DATE_TEST_JPG = "src/test-integration/resources/date-test.jpg";
    @Autowired
    private JwsBuilder jwsBuilder;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserImageService userImageService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql"})
    public void createUserImage_shouldReturnSuccess() throws Exception {
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);

        Path path = Paths.get(RESOURCES_DATE_TEST_JPG).toAbsolutePath();
        var fileBytes = Files.readAllBytes(path);

        var multipartFile = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile2 = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile3 = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);

        // when
        var mvcResult = mockMvc.perform(multipart("/users/images")
                        .file(multipartFile)
                        .file(multipartFile2)
                        .file(multipartFile3)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        UserImageCreatedResponse userImageCreatedResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserImageCreatedResponse.class);
        assertThat(userImageCreatedResponse.imageIds()).size().isEqualTo(3);
        assertThat(userImageCreatedResponse.userId()).isEqualTo(userId);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql"})
    public void getUserImage_shouldReturnSuccess() throws Exception {
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);

        UserImageResponse userImageResponse = userImageService.getUserImages(userId);
        assertThat(userImageResponse.userImageData().size()).isEqualTo(0);

        Path path = Paths.get(RESOURCES_DATE_TEST_JPG).toAbsolutePath();
        var fileBytes = Files.readAllBytes(path);

        var multipartFile = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile2 = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile3 = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);

        userImageService.createUserImages(UUID.fromString(userId), List.of(multipartFile, multipartFile2, multipartFile3));

        // when
        var mvcResultGet = mockMvc.perform(get("/users/{userid}/images", userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        UserImageResponse getUserImageResponse = objectMapper.readValue(mvcResultGet.getResponse().getContentAsString(), UserImageResponse.class);
        assertThat(getUserImageResponse.userImageData()).size().isEqualTo(3);
        assertThat(getUserImageResponse.userId()).isEqualTo(userId);
    }

}