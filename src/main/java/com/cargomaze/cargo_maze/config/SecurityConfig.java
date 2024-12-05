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

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//            .csrf().disable()
//            .cors()
//            .and()
//            .authorizeRequests()
//            .requestMatchers("/public/**").permitAll() // Rutas públicas
//            .anyRequest().authenticated() // Todas las demás requieren autenticación
//            .and()
//            .oauth2Login()
////            .successHandler((request, response, authentication) -> {
////                // Redirigir al endpoint del controlador después del login exitoso
////                response.sendRedirect("/cargoMaze/correct");
////            })
//            .failureHandler((request, response, exception) -> {
//                // Manejar fallos de autenticación
//                response.sendRedirect("http://localhost:4200?error=true");
//            });
//        return http.build();
//    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
