package com.muhimbili.labnotification.configation.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOriginPatterns;
    private final String[] allowedMethods;
    private final String[] allowedHeaders;
    private final boolean allowCredentials;

    public CorsConfig(
        @Value("${app.cors.allowed-origins:*}") String allowedOrigins,
        @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}") String allowedMethods,
        @Value("${app.cors.allowed-headers:*}") String allowedHeaders,
        @Value("${app.cors.allow-credentials:true}") boolean allowCredentials
    ) {
        this.allowedOriginPatterns = toArray(allowedOrigins);
        this.allowedMethods = toArray(allowedMethods);
        this.allowedHeaders = toArray(allowedHeaders);
        this.allowCredentials = allowCredentials;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(allowedOriginPatterns)
            .allowedMethods(allowedMethods)
            .allowedHeaders(allowedHeaders)
            .allowCredentials(allowCredentials)
            .maxAge(3600);
    }

    private String[] toArray(String value) {
        List<String> parsed = Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
        return parsed.toArray(String[]::new);
    }
}
