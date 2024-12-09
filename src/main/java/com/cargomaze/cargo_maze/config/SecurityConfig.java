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
        "https://login.microsoftonline.com/ac3a534a-d5d6-42f6-aa4f-9dd5fbef911f/oauth2/v2.0/authorize",
        "https://login.microsoftonline.com",
        "https://calm-rock-0d4eb650f.5.azurestaticapps.net"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF si no es necesario para tu caso
            .cors(cors -> cors.disable()) // Deshabilitar CORS completamente
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll() // Rutas públicas permitidas
                .anyRequest().authenticated() // Cualquier otra ruta necesita autenticación
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler((request, response, authentication) -> response.sendRedirect("/cargoMaze/correct"))
                .failureHandler((request, response, exception) -> response.sendRedirect("https://calm-rock-0d4eb650f.5.azurestaticapps.net?error=true"))
            );
    return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(ALLOWED_ORIGINS) // Permite todos los orígenes (útil solo para pruebas, no en producción)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization", "Link", "X-Total-Count") // Opcional: expone encabezados adicionales
                        .allowCredentials(true); // Cambiar a true si necesitas enviar cookies o credenciales
            }
        };
    }

}
