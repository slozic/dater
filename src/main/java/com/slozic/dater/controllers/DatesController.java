package com.slozic.dater.controllers;

import com.slozic.dater.dto.DateEventDto;
import com.slozic.dater.dto.MyDateEventDto;
import com.slozic.dater.dto.enums.JoinDateStatus;
import com.slozic.dater.dto.request.CreateDateEventRequest;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.models.DateImage;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.repositories.DateImageRepository;
import com.slozic.dater.repositories.DateRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dates")
@RequiredArgsConstructor
@Slf4j
//@CrossOrigin(origins = "http://localhost:3000", originPatterns = "*", allowedHeaders = "*", methods = {RequestMethod.POST, RequestMethod.GET})
public class DatesController {

    private final DateRepository dateRepository;
    private final DateAttendeeRepository dateAttendeeRepository;
    private final DateImageRepository dateImageRepository;

    @GetMapping
    public List<DateEventDto> getAllDates() throws UnauthorizedException {
        final UUID currentUser = JwtAuthenticatedUserService.getCurrentUserOrThrow();
        final List<Date> dateList = dateRepository.findAll();
        return dateList.stream()
                       .filter(date -> !date.getCreatedBy().equals(currentUser))
                       .map(date -> new DateEventDto(date.getId().toString(), date.getTitle(), date.getLocation(),
                                                     date.getDescription(), date.getUser().getUsername(), "", date.getScheduledTime().toString(),
                       ""))
                       .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public DateEventDto getDateById(@PathVariable("id") final String dateId) throws UnauthorizedException {
        final Date dateEvent = dateRepository.findById(UUID.fromString(dateId)).orElseGet(Date::new);
        final Optional<DateAttendee> optionalDateAttendee = dateAttendeeRepository.findOneByAttendeeIdAndDateId(JwtAuthenticatedUserService.getCurrentUserOrThrow(), UUID.fromString(dateId));
        return List.of(dateEvent).stream()
                       .map(date -> new DateEventDto(
                               date.getId().toString(),
                               date.getTitle(),
                               date.getLocation(),
                               date.getDescription(),
                               date.getUser().getUsername(),
                               "",
                               date.getScheduledTime().toString(), getJoinDateStatus(optionalDateAttendee).toString()

                       ))
                       .collect(Collectors.toList()).stream().findFirst().get();
    }

    private JoinDateStatus getJoinDateStatus(final Optional<DateAttendee> optionalDateAttendee) {
        if(optionalDateAttendee.isEmpty()){
            return JoinDateStatus.AVAILABLE;
        }
        if (optionalDateAttendee.isPresent() & optionalDateAttendee.get().getAccepted()) {
            return JoinDateStatus.ACCEPTED;
        } else if (!optionalDateAttendee.get().getAccepted()) {
            return JoinDateStatus.PENDING;
        }
        return JoinDateStatus.AVAILABLE;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createDateEvent(@RequestParam("title") String title,
                                             @RequestParam("location") String location,
                                             @RequestParam("description") String description,
                                             @RequestParam("scheduledTime") String scheduledTime,
                                             @RequestPart("image1") MultipartFile image1) throws UnauthorizedException {
        try {
            Date date = Date.builder()
                    .title(title)
                    .description(location)
                    .location(description)
                    .scheduledTime(OffsetDateTime.of(LocalDateTime.parse(scheduledTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME), ZoneOffset.UTC))
                    .createdBy(JwtAuthenticatedUserService.getCurrentUserOrThrow())
                    .build();
            final Date dateCreated = dateRepository.save(date);
            DateAttendee dateAttendee = DateAttendee.builder()
                    .dateId(dateCreated.getId())
                    .attendeeId(dateCreated.getCreatedBy())
                    .accepted(true)
                    .build();
            dateAttendeeRepository.save(dateAttendee);
            String file = storeFile(image1);
            DateImage dateImage = DateImage.builder()
                    .dateId(date.getId())
                    .imagePath(file)
                    .imageSize(1024000)
                    .build();
            dateImageRepository.save(dateImage);

            // Return a success response
            Map<String, String> response = new HashMap<>();
            response.put("message", "Date event created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return an error response in case of any exception
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create date event");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private String storeFile(final MultipartFile image) {
        String originalFilename = image.getOriginalFilename();
        Long fileSize = image.getSize();
        String contentType = image.getContentType();
        File imageDir = new File("C:\\Users\\sly-x\\projects\\spring\\dater-images");
        File file = new File(imageDir.getPath() + "\\" + System.currentTimeMillis() + "" + RandomGenerator.getDefault().nextInt() + ".png");
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file.getPath();
    }

    @GetMapping("/user/date")
    public List<MyDateEventDto> getDatesByCurrentUser() throws UnauthorizedException {
        final UUID currentUser = JwtAuthenticatedUserService.getCurrentUserOrThrow();
        final List<DateAttendee> dateList = dateRepository.findAllCreatedByUserAndRequestedByUser(currentUser);
        return dateList.stream()
                       .map(dateAttendee -> new MyDateEventDto(dateAttendee.getDateId().toString(), dateAttendee.getDate().getTitle(), dateAttendee.getDate().getLocation(),
                                                     dateAttendee.getDate().getDescription(), dateAttendee.getUser().getUsername(), "", dateAttendee.getDate().getScheduledTime().toString(),
                                                     "", dateAttendee.getDate().getCreatedBy().equals(currentUser)))
                       .collect(Collectors.toList());
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImageByDateId(@PathVariable("id") String dateId, @RequestParam("resize") boolean resize) throws IOException {
        List<DateImage> dateImageList = dateImageRepository.findAllByDateId(UUID.fromString(dateId));
        DateImage dateImage = dateImageList.stream().findFirst().orElseThrow(() -> new RuntimeException());
        // Read the image file
        File file = new File(dateImage.getImagePath());
        byte[] imageBytes = Files.readAllBytes(file.toPath());

        if(resize){
            imageBytes = resizeImage(imageBytes);
        }

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        // Return the image bytes along with headers
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    private byte[] resizeImage(byte[] imageBytes) throws IOException {
        // Resize the image
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(new ByteArrayInputStream(imageBytes))
                    .size(400, 400)
                    .outputFormat("png") // or "png" or any other format you want
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        }
    }

}
