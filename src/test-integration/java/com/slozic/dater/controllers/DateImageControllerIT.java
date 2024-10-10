package com.slozic.dater.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slozic.dater.dto.response.images.DateImageCreatedResponse;
import com.slozic.dater.dto.response.images.DateImageDeletedResponse;
import com.slozic.dater.dto.response.images.DateImageMetaData;
import com.slozic.dater.dto.response.images.DateImageResponse;
import com.slozic.dater.exceptions.DateEventException;
import com.slozic.dater.exceptions.DateImageException;
import com.slozic.dater.models.DateImage;
import com.slozic.dater.services.DateEventImageService;
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(JwsBuilder.class)
class DateImageControllerIT extends IntegrationTest {
    private static final String RESOURCES_DATE_TEST_JPG = "src/test-integration/resources/date-test.jpg";
    private static final String RESOURCES_DATE_INVALID_FORMAT = "src/test-integration/resources/date-invalid-file.txt";
    @Autowired
    private JwsBuilder jwsBuilder;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DateEventImageService dateEventImageService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql"})
    public void createDateEventImages_shouldReturnSuccess() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);
        String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        Path path = Paths.get(RESOURCES_DATE_TEST_JPG).toAbsolutePath();
        var fileBytes = Files.readAllBytes(path);

        var multipartFile = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile2 = new MockMultipartFile("files", "image2.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile3 = new MockMultipartFile("files", "image3.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);

        List<DateImageMetaData> dateEventImageMetaData = dateEventImageService.getDateEventImageMetaData(dateId);
        assertThat(dateEventImageMetaData).size().isEqualTo(0);

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
        DateImageCreatedResponse dateImageCreatedResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DateImageCreatedResponse.class);
        assertThat(dateImageCreatedResponse.imageIds()).size().isEqualTo(3);
        assertThat(dateImageCreatedResponse.dateId()).isEqualTo(dateId);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql"})
    public void createDateEventImages_shouldFailWithNonExistingDateId() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);
        String dateId = UUID.randomUUID().toString();

        Path path = Paths.get(RESOURCES_DATE_TEST_JPG).toAbsolutePath();
        var fileBytes = Files.readAllBytes(path);
        var multipartFile = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);

        // when
        var mvcResult = mockMvc.perform(multipart("/dates/{dateId}/images", dateId)
                        .file(multipartFile)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn();

        // then
        assertThat(mvcResult.getResolvedException() instanceof DateEventException).isTrue();
        assertThat(mvcResult.getResolvedException().getMessage()).isEqualTo("No DateEvents found with id: " + dateId);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql"})
    public void createDateEventImages_shouldFailWithUnsupportedFiletype() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);
        String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        Path path = Paths.get(RESOURCES_DATE_INVALID_FORMAT).toAbsolutePath();
        var fileBytes = Files.readAllBytes(path);
        var multipartFile = new MockMultipartFile("files", "image1.jpg", MediaType.TEXT_PLAIN_VALUE, fileBytes);

        // when
        var mvcResult = mockMvc.perform(multipart("/dates/{dateId}/images", dateId)
                        .file(multipartFile)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn();

        // then
        assertThat(mvcResult.getResolvedException() instanceof DateImageException).isTrue();
        assertThat(mvcResult.getResolvedException().getMessage()).isEqualTo("Unsupported file type text/plain");
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql"})
    public void createDateEventImages_shouldFailWithTooManyImagesUploaded() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);
        String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        Path path = Paths.get(RESOURCES_DATE_TEST_JPG).toAbsolutePath();
        var fileBytes = Files.readAllBytes(path);

        var multipartFile = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile2 = new MockMultipartFile("files", "image2.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile3 = new MockMultipartFile("files", "image3.jpg", MediaType.IMAGE_PNG_VALUE, fileBytes);
        var multipartFile4 = new MockMultipartFile("files", "image4.jpg", MediaType.IMAGE_PNG_VALUE, fileBytes);

        // when
        var mvcResult = mockMvc.perform(multipart("/dates/{dateId}/images", dateId)
                        .file(multipartFile)
                        .file(multipartFile2)
                        .file(multipartFile3)
                        .file(multipartFile4)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn();

        // then
        assertThat(mvcResult.getResolvedException() instanceof DateImageException).isTrue();
        assertThat(mvcResult.getResolvedException().getMessage()).isEqualTo("You can have only up to 3 images per date event!");
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql"})
    public void getDateEventImages_shouldReturnSuccess() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);
        String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        // verify there are no image entities associated with date
        List<DateImageMetaData> dateEventImageMetaData = dateEventImageService.getDateEventImageMetaData(dateId);
        assertThat(dateEventImageMetaData).size().isEqualTo(0);

        // insert new images
        Path path = Paths.get(RESOURCES_DATE_TEST_JPG).toAbsolutePath();
        var fileBytes = Files.readAllBytes(path);

        var multipartFile = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile2 = new MockMultipartFile("files", "image2.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile3 = new MockMultipartFile("files", "image3.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);

        dateEventImageService.createDateEventImages(dateId, List.of(multipartFile, multipartFile2, multipartFile3));

        // when
        var mvcResultGet = mockMvc.perform(get("/dates/{dateId}/images", dateId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        // then
        DateImageResponse dateImageResponseActual = objectMapper.readValue(mvcResultGet.getResponse().getContentAsString(), DateImageResponse.class);
        assertThat(dateImageResponseActual.dateImageData()).size().isEqualTo(3);
        assertThat(dateImageResponseActual.dateId()).isEqualTo(dateId);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql"})
    public void getDateEventImages_shouldReturnEmptyListForNonExistingDateId() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);
        String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        var mvcResultGet = mockMvc.perform(get("/dates/{dateId}/images", dateId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        // then
        assertThat(mvcResultGet.getResponse().getContentLength()).isEqualTo(0);
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql"})
    public void deleteDateEventImage_shouldReturnSuccess() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);
        String dateId = "be62daa9-6cda-45ea-8b0b-4ea15f735e53";

        Path path = Paths.get(RESOURCES_DATE_TEST_JPG).toAbsolutePath();
        var fileBytes = Files.readAllBytes(path);

        var multipartFile = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);
        var multipartFile2 = new MockMultipartFile("files", "image2.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);

        DateImageCreatedResponse dateEventImages = dateEventImageService.createDateEventImages(dateId, List.of(multipartFile, multipartFile2));
        String imageIdToDelete = dateEventImages.imageIds().get(0);
        DateImageMetaData imageMetaData = dateEventImageService.getDateEventImageMetaData(dateId).stream().filter(dateImage -> dateImage.id().equals(imageIdToDelete)).findFirst().get();

        // when
        var mvcResult = mockMvc.perform(delete("/dates/{dateId}/images/{imageId}", dateId, imageIdToDelete)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        // then
        DateImageDeletedResponse dateImageDeletedResponse = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), DateImageDeletedResponse.class);
        assertThat(dateImageDeletedResponse.imageId()).isEqualTo(imageIdToDelete);
        assertThat(dateImageDeletedResponse.dateId()).isEqualTo(dateId);
        assertThat(new File(imageMetaData.path()).exists()).isFalse();
    }

    @Test
    @Sql(scripts = {"classpath:fixtures/resetDB.sql",
            "classpath:fixtures/loadUsers.sql",
            "classpath:fixtures/loadDateEvents.sql"})
    public void deleteDateEventImage_shouldFailWhenUserIsNotDateCreator() throws Exception {
        // given
        String userId = "aae884f1-e3bc-4c48-8ebb-adb6f6dfc5d5";
        String token = jwsBuilder.getJwt(userId);
        String dateIdFromOtherUser = "c7404d30-1edf-4334-97b8-b03c668b70b9";

        Path path = Paths.get(RESOURCES_DATE_TEST_JPG).toAbsolutePath();
        var fileBytes = Files.readAllBytes(path);
        var multipartFile = new MockMultipartFile("files", "image1.jpg", MediaType.IMAGE_JPEG_VALUE, fileBytes);

        DateImageCreatedResponse dateEventImages = dateEventImageService.createDateEventImages(dateIdFromOtherUser, List.of(multipartFile));
        String imageIdToDelete = dateEventImages.imageIds().get(0);

        // when
        mockMvc.perform(delete("/dates/{dateId}/images/{imageId}", dateIdFromOtherUser, imageIdToDelete)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof DateImageException))
                .andReturn();
        // then
    }

}