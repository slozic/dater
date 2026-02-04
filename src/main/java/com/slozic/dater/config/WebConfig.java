package com.slozic.dater.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${user.images.location}")
    private String userImagesLocation;
    @Value("${date.images.location}")
    private String dateImagesLocation;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/media/user/**")
                .addResourceLocations(toResourceLocation(userImagesLocation));
        registry.addResourceHandler("/media/date/**")
                .addResourceLocations(toResourceLocation(dateImagesLocation));
    }

    private String toResourceLocation(String location) {
        String resourceLocation = Path.of(location).toUri().toString();
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }
        return resourceLocation;
    }
}
