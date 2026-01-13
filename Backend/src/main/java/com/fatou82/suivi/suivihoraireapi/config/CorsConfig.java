package com.fatou82.suivi.suivihoraireapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // S'applique à toutes les routes de l'API
                        .allowedOrigins("http://localhost:4200") // Autorise ton front Angular
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // Autorise toutes les méthodes
                        .allowedHeaders("*") // Autorise tous les headers (important pour le JWT)
                        .allowCredentials(true); // Autorise l'envoi de cookies ou d'auth si besoin
            }
        };
    }
}