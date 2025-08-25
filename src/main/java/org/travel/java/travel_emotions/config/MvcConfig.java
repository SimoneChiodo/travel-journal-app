package org.travel.java.travel_emotions.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer { // Makes uploaded files (images and videos) accessible via URL
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
      registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
    }
}
