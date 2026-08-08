package com.society.config;

import com.society.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration @EnableWebSecurity @EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthEntryPoint       jwtAuthEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            // Wires the CorsConfigurationSource bean (see CorsConfig) directly into
            // the security filter chain. Without this, Spring Security has no idea
            // CORS exists and rejects cross-origin preflight requests before they
            // ever reach Spring MVC's CORS handling - this was the root cause of
            // "login works, then the user is instantly logged out".
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Belt-and-braces: always let CORS preflight requests through,
                // regardless of which endpoint they target. Browsers never send
                // credentials/auth headers on a preflight, so requiring auth here
                // would always fail it.
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public auth endpoints. Public self-registration (society, and
                // resident/staff picking a society) is intentionally allowed here —
                // it's what makes this a self-service multi-tenant platform — but
                // every self-registered account/tenant starts inactive/PENDING and
                // requires an explicit approval step (SUPER_ADMIN for societies,
                // Society Admin for residents/staff) before it can do anything.
                .requestMatchers("/api/auth/login", "/api/auth/captcha", "/api/auth/register-public",
                                  "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
