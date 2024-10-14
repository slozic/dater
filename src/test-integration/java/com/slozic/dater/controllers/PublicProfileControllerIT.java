package com.slozic.dater.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slozic.dater.dto.response.PublicProfileResponse;
import com.slozic.dater.services.images.ProfileImageService;
import com.slozic.dater.services.user.PublicProfileService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(JwsBuilder.class)
class PublicProfileControllerIT extends IntegrationTest {
    private static final String RESOURCES_DATE_TEST_JPG = "src/test-integration/resources/date-test.jpg";
    @Autowired
    private JwsBuilder jwsBuilder;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PublicProfileService publicProfileService;
    @Autowired
    private ProfileImageService profileImageService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql"})
    void getPublicProfile_shouldReturnSuccess() throws Exception {
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String publicProfileId = "6c49abd4-0e82-47f6-bb0c-558c9a890bd4";
        String token = jwsBuilder.getJwt(userId);

        Path path = Paths.get(RESOURCES_DATE_TEST_JPG).toAbsolutePath();
        var fileBytes = Files.readAllBytes(path);

        var multipartFile = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile2 = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile3 = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);

        profileImageService.createProfileImages(publicProfileId, List.of(multipartFile, multipartFile2, multipartFile3));

        // when
        var mvcResultGet = mockMvc.perform(get("/users/{id}/public-profile", publicProfileId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        PublicProfileResponse publicProfileResponse = objectMapper.readValue(mvcResultGet.getResponse().getContentAsString(), PublicProfileResponse.class);
        assertThat(publicProfileResponse.profileImageData()).size().isEqualTo(3);
        assertThat(publicProfileResponse.userId()).isEqualTo(publicProfileId);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql"})
    void getPublicProfile_shouldFailWhenUserDoesNotExist() throws Exception {
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String nonExistentProfileId = UUID.randomUUID().toString();
        String token = jwsBuilder.getJwt(userId);

        // when
        var mvcResultGet = mockMvc.perform(get("/users/{id}/public-profile", nonExistentProfileId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

}