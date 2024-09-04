package com.slozic.dater.dto.request;

import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public record CreateDateEventRequest (String title, String description, String location, String scheduledTime, Optional<MultipartFile> image, String dateCreator){};
