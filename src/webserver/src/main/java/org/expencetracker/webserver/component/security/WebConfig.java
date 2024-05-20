package org.expencetracker.webserver.component.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map the /public/images/ URL path to the local ./public/images/ directory
        registry.addResourceHandler("/public/images/**")
                .addResourceLocations("file:./public/images/");
    }
}