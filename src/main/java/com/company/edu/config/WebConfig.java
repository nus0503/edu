package com.company.edu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        CacheControl cacheControl = CacheControl.maxAge(Duration.ofMinutes(1)).cachePublic();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:C:/Users/nus05/uploads/")
                .setCacheControl(cacheControl);
    }
}
