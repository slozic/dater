package com.slozic.dater.services;

import com.slozic.dater.dto.DateEventDto;
import com.slozic.dater.dto.MyDateEventDto;
import com.slozic.dater.dto.enums.JoinDateStatus;
import com.slozic.dater.exceptions.DateEventException;
import com.slozic.dater.exceptions.UnauthorizedException;
import com.slozic.dater.models.Date;
import com.slozic.dater.models.DateAttendee;
import com.slozic.dater.models.DateImage;
import com.slozic.dater.repositories.DateAttendeeRepository;
import com.slozic.dater.repositories.DateImageRepository;
import com.slozic.dater.repositories.DateRepository;
import com.slozic.dater.security.JwtAuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DateService {
    private static final String DEFAULT_IMAGES_LOCATION = "C:\\Users\\sly-x\\projects\\spring\\dater-images";
    private static final int DEFAULT_IMAGE_RESIZE_WIDTH_HEIGHT = 400;
    private static final String DEFAULT_IMAGE_RESIZE_TYPE = "png";
    private final DateRepository dateRepository;
    private final DateAttendeeRepository dateAttendeeRepository;
    private final DateImageRepository dateImageRepository;
    private final JwtAuthenticatedUserService jwtAuthenticatedUserService;

    public List<DateEventDto> getDateEventDtos() {
        final List<Date> dateList = dateRepository.findAll();

        return dateList.stream()
                .map(date -> new DateEventDto(
                        date.getId().toString(),
                        date.getTitle(),
                        date.getLocation(),
                        date.getDescription(),
                        date.getUser().getUsername(), "",
                        date.getScheduledTime().toString(),
                        ""))
                .collect(Collectors.toList());
    }

    public DateEventDto getDateEventDto(String dateId) throws UnauthorizedException {
        final Date dateEvent = dateRepository.findById(UUID.fromString(dateId)).orElseGet(Date::new);
        final UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        final Optional<DateAttendee> optionalDateAttendee = dateAttendeeRepository.findOneByAttendeeIdAndDateId(currentUser, UUID.fromString(dateId));

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
        if (optionalDateAttendee.isPresent()) {
            if (optionalDateAttendee.get().getAccepted())
                return JoinDateStatus.ACCEPTED;
            else
                return JoinDateStatus.PENDING;
        }
        return JoinDateStatus.AVAILABLE;
    }

    public UUID createDateEventFromRequest(String title, String location, String description, String scheduledTime, Optional<MultipartFile> image1) throws DateEventException {
        try {
            final Date dateCreated = createDateEvent(title, location, description, scheduledTime);
            createDefaultDateAttendee(dateCreated);
            createDateEventImage(image1, dateCreated);

            return dateCreated.getId();
        } catch (Exception e) {
            throw new DateEventException("Unable to create DateEvent");
        }
    }

    private void createDateEventImage(Optional<MultipartFile> image1, Date dateCreated) {
        if (image1.isPresent()) {
            MultipartFile multipartFile = image1.get();
            File file = storeFile(multipartFile);
            DateImage dateImage = DateImage.builder()
                    .dateId(dateCreated.getId())
                    .imagePath(file.getPath())
                    .imageSize((int) multipartFile.getSize())
                    .build();
            dateImageRepository.save(dateImage);
        }
    }

    private DateAttendee createDefaultDateAttendee(Date dateCreated) {
        DateAttendee dateAttendee = DateAttendee.builder()
                .dateId(dateCreated.getId())
                .attendeeId(dateCreated.getCreatedBy())
                .accepted(true)
                .build();
        return dateAttendeeRepository.save(dateAttendee);
    }

    private Date createDateEvent(String title, String location, String description, String scheduledTime) throws UnauthorizedException {
        UUID currentUser = jwtAuthenticatedUserService.getCurrentUserOrThrow();
        Date date = Date.builder()
                .title(title)
                .description(description)
                .location(location)
                .scheduledTime(OffsetDateTime.of(LocalDateTime.parse(scheduledTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME), ZoneOffset.UTC))
                .createdBy(currentUser)
                .build();
        final Date dateCreated = dateRepository.save(date);

        return dateCreated;
    }

    private File storeFile(final MultipartFile image) {
        File imageDir = new File(DEFAULT_IMAGES_LOCATION);
        File file = new File(imageDir.getPath() + "\\" + System.currentTimeMillis() + RandomGenerator.getDefault().nextInt() + "." + DEFAULT_IMAGE_RESIZE_TYPE);
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    private byte[] resizeImage(byte[] imageBytes) throws IOException {
        // Resize the image
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(new ByteArrayInputStream(imageBytes))
                    .size(DEFAULT_IMAGE_RESIZE_WIDTH_HEIGHT, DEFAULT_IMAGE_RESIZE_WIDTH_HEIGHT)
                    .outputFormat(DEFAULT_IMAGE_RESIZE_TYPE)
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        }
    }

    public List<MyDateEventDto> getMyDateEventDtos(UUID currentUser) {
        final List<DateAttendee> dateList = dateAttendeeRepository.findAllCreatedByUserAndRequestedByUser(currentUser);

        return dateList.stream()
                .map(dateAttendee -> new MyDateEventDto(
                        dateAttendee.getDateId().toString(),
                        dateAttendee.getDate().getTitle(),
                        dateAttendee.getDate().getLocation(),
                        dateAttendee.getDate().getDescription(),
                        dateAttendee.getUser().getUsername(), "",
                        dateAttendee.getDate().getScheduledTime().toString(),
                        dateAttendee.getDate().getCreatedBy().equals(currentUser)))
                .collect(Collectors.toList());
    }

    public byte[] getImageBytes(String dateId, boolean shouldResize) throws IOException {
        List<DateImage> dateImageList = dateImageRepository.findAllByDateId(UUID.fromString(dateId));
        DateImage dateImage = dateImageList.stream().findFirst().orElseThrow(() -> new RuntimeException());
        // Read the image file
        File file = new File(dateImage.getImagePath());
        byte[] imageBytes = Files.readAllBytes(file.toPath());

        if (shouldResize) {
            imageBytes = resizeImage(imageBytes);
        }
        return imageBytes;
    }
}
