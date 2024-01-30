package com.slozic.dater.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record CreateDateEventRequest (String title, String description, String location, String scheduledTime, MultipartFile images){};
