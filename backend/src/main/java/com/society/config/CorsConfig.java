package com.society.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            // allowedOriginPatterns (not allowedOrigins) supports wildcards like
            // http://192.168.*.*:5173 while still working with allowCredentials(true) -
            // this is what lets other devices on your LAN reach the API, not just localhost.
            .allowedOriginPatterns(allowedOrigins.split(","))
            .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
