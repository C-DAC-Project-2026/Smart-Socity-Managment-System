package com.society.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * IMPORTANT: this must be exposed as a CorsConfigurationSource *bean* (not a
 * WebMvcConfigurer#addCorsMappings override) because Spring Security's filter
 * chain runs before Spring MVC ever sees the request. A WebMvcConfigurer-only
 * CORS setup is invisible to Spring Security, so every cross-origin preflight
 * (OPTIONS) request to a protected endpoint gets rejected by the security
 * filter chain with 401/403 before MVC's CORS handling ever runs. In the
 * browser this shows up as a blocked/failed request with no response body -
 * which the frontend's axios interceptor (see api/axios.js) treats as
 * "unauthorized/unreachable" and immediately logs the user out. That is what
 * was previously causing "login succeeds, then user is instantly logged out
 * again" for every role, most noticeably for Admin/Resident/Staff dashboards
 * that fire off several authenticated requests right after login.
 *
 * This bean is picked up by SecurityConfig via http.cors(...), which wires
 * CORS handling directly into the security filter chain, before the
 * authorization checks run.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // allowedOriginPatterns (not allowedOrigins) supports wildcards like
        // http://192.168.*.*:5173 while still working with allowCredentials(true) -
        // this is what lets other devices on your LAN reach the API, not just localhost.
        config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
