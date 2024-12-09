package com.cargomaze.cargo_maze.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String[] ALLOWED_ORIGINS = {
        "http://localhost:4200",
        "http://localhost:8080",
        "https://calm-rock-0d4eb650f.5.azurestaticapps.net",
        "https://proyectoarsw.duckdns.org",
        "https://login.microsoftonline.com"
    };


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF si no es necesario
            .oauth2Login(oauth2 -> oauth2
                .successHandler((request, response, authentication) -> {
                    System.out.println("Autenticación exitosa, redirigiendo...");
                    response.sendRedirect("/cargoMaze/correct");
                })
                .failureHandler((request, response, exception) -> {
                    System.err.println("Error de autenticación: " + exception.getMessage());
                    response.sendRedirect("https://calm-rock-0d4eb650f.5.azurestaticapps.net?error=true");
                })
            );
        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(ALLOWED_ORIGINS)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true); // Permitir credenciales (cookies)
            }
        };
    }
}
