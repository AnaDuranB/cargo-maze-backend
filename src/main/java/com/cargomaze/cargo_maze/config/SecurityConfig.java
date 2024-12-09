package com.cargomaze.cargo_maze.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
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
        "http://pollos.eastus2.cloudapp.azure.com",
        "https://login.microsoftonline.com/ac3a534a-d5d6-42f6-aa4f-9dd5fbef911f/oauth2/v2.0/authorize",
        "https://login.microsoftonline.com",
        "https://calm-rock-0d4eb650f.5.azurestaticapps.net",
        "https://cargo-maze-backend-hwgpaheeb7hreqgv.eastus2-01.azurewebsites.net",
        "https://cargo-maze-backend2-gbaadrdgb9eqf9e6.eastus2-01.azurewebsites.net",
        "https://proyectoarsw.duckdns.org",
        "https://135.232.42.21"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF si no es necesario
            .cors(Customizer.withDefaults()) // Habilitar CORS con la configuración del Bean
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll() // Rutas públicas
                .anyRequest().authenticated() // Otras rutas requieren autenticación
            )
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
                        .exposedHeaders("Authorization", "Link", "X-Total-Count") // Encabezados adicionales
                        .allowCredentials(true); // Permitir credenciales (cookies)
            }
        };
    }
}

