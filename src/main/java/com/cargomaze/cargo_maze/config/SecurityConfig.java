package com.cargomaze.cargo_maze.config;


import java.util.Arrays;

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
                    .requestMatchers("/login/**", "/stompendpoint/**", "/auth/**").permitAll()
                    .requestMatchers("/cargoMaze/**").authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler((request, response, authentication) -> {
                    System.out.println("Autenticación exitosa, redirigiendo...");
                    response.sendRedirect("/auth");
                })
                .failureHandler((request, response, exception) -> {
                    System.err.println("Error de autenticación: " + exception.getMessage());
                    response.sendRedirect("https://calm-rock-0d4eb650f.5.azurestaticapps.net?error=true");
                })
            );
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Arrays.asList(ALLOWED_ORIGINS));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));  // Headers permitidos

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
