package com.cargomaze.cargo_maze.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF si no es necesario para tu caso
            .cors(cors -> cors.disable()) // Deshabilitar CORS completamente
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/**").permitAll() // Rutas públicas permitidas
                .anyRequest().authenticated() // Cualquier otra ruta necesita autenticación
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler((request, response, authentication) -> response.sendRedirect("/cargoMaze/correct"))
                .failureHandler((request, response, exception) -> response.sendRedirect("https://calm-rock-0d4eb650f.5.azurestaticapps.net?error=true"))
            );
    return http.build();
    }
}


