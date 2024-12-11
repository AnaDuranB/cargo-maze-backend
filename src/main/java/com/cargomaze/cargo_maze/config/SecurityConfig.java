package com.cargomaze.cargo_maze.config;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;



@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String[] ALLOWED_ORIGINS = {
        "https://calm-rock-0d4eb650f.5.azurestaticapps.net",
        "http://localhost:4200",
        "http://localhost:8080",
        "https://proyectoarsw.duckdns.org",
        "https://login.microsoftonline.com"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)  // Deshabilitar CSRF si no es necesario
            .authorizeHttpRequests(req  -> req
                    .requestMatchers(HttpMethod.OPTIONS).permitAll()
                    .requestMatchers("/login/**", "/stompendpoint/**", "/auth/**", "/cargoMaze/test-encryption", "/error").permitAll()
                    .requestMatchers("/cargoMaze/**").permitAll()
            );
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true); // Permitir cookies
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:4200/*",
                "http://localhost:8080/*",
                "https://calm-rock-0d4eb650f.5.azurestaticapps.net/*",
                "https://proyectoarsw.duckdns.org/*",
                "https://login.microsoftonline.com/*",
                "http://localhost:4200",
                "https://calm-rock-0d4eb650f.5.azurestaticapps.net",
                "https://cargo-maze-backend-hwgpaheeb7hreqgv.eastus2-01.azurewebsites.net",
                "https://cargo-maze-backend-hwgpaheeb7hreqgv.eastus2-01.azurewebsites.net/"

        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Métodos permitidos
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "*")); // Headers permitidos
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition")); // Headers expuestos

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

    
