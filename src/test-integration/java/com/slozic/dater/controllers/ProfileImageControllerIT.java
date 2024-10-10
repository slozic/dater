package com.slozic.dater.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slozic.dater.dto.response.userprofile.ProfileImageCreatedResponse;
import com.slozic.dater.dto.response.userprofile.ProfileImageResponse;
import com.slozic.dater.services.images.ProfileImageService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(JwsBuilder.class)
class ProfileImageControllerIT extends IntegrationTest {

    private static final String RESOURCES_DATE_TEST_JPG = "src/test-integration/resources/date-test.jpg";
    @Autowired
    private JwsBuilder jwsBuilder;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProfileImageService profileImageService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql"})
    public void createUserProfileImage_shouldReturnSuccess() throws Exception {
        // given
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

        // then
        ProfileImageCreatedResponse profileImageCreatedResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ProfileImageCreatedResponse.class);
        assertThat(profileImageCreatedResponse.imageIds()).size().isEqualTo(3);
        assertThat(profileImageCreatedResponse.userId()).isEqualTo(userId);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql"})
    public void getUserProfileImage_shouldReturnSuccess() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);

        ProfileImageResponse profileImageResponse = profileImageService.getProfileImages(userId);
        assertThat(profileImageResponse.profileImageData().size()).isEqualTo(0);

        Path path = Paths.get(RESOURCES_DATE_TEST_JPG).toAbsolutePath();
        var fileBytes = Files.readAllBytes(path);

        var multipartFile = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile2 = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile3 = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);

        profileImageService.createProfileImages(UUID.fromString(userId), List.of(multipartFile, multipartFile2, multipartFile3));

        // when
        var mvcResultGet = mockMvc.perform(get("/users/images")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        // then
        ProfileImageResponse getProfileImageResponse = objectMapper.readValue(mvcResultGet.getResponse().getContentAsString(), ProfileImageResponse.class);
        assertThat(getProfileImageResponse.profileImageData()).size().isEqualTo(3);
        assertThat(getProfileImageResponse.userId()).isEqualTo(userId);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql"})
    public void deleteUserProfileImage_shouldReturnSuccess() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);

        Path path = Paths.get(RESOURCES_DATE_TEST_JPG).toAbsolutePath();
        var fileBytes = Files.readAllBytes(path);

        var multipartFile = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile2 = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile3 = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);

        ProfileImageCreatedResponse profileImages = profileImageService.createProfileImages(UUID.fromString(userId), List.of(multipartFile, multipartFile2, multipartFile3));

        // when
        var mvcResultGet = mockMvc.perform(delete("/users/images/{imageId}", profileImages.imageIds().get(0))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        // then
        Boolean booleanResponse = objectMapper.readValue(mvcResultGet.getResponse().getContentAsString(), Boolean.class);
        assertThat(booleanResponse).isTrue();
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql"})
    public void deleteUserProfileImage_shouldFailWhenImageDoesNotExist() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);

        // when
        var mvcResultGet = mockMvc.perform(delete("/users/images/{imageId}", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

}