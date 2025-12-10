package com.fatou82.suivi.suivihoraireapi.config;

import com.fatou82.suivi.suivihoraireapi.config.JwtAuthFilter;
import com.fatou82.suivi.suivihoraireapi.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthService authService; 
    
    // 1. Définition de l'encodeur de mot de passe
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // 2. Bean pour gérer le processus d'authentification
    // Nous exposons explicitement un AuthenticationManager basé sur notre DaoAuthenticationProvider
    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider daoAuthenticationProvider) {
        return new ProviderManager(java.util.List.of(daoAuthenticationProvider));
    }
    
    // 3. Bean pour le DaoAuthenticationProvider (Nécessaire pour que Spring sache comment s'authentifier)
    // Spring utilisera ce Bean pour la configuration automatique de l'AuthenticationManager.
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder) {
        
        // Dans les versions récentes de Spring Security, le constructeur prend le UserDetailsService
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(authService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }


    // 4. Configuration de la chaîne de filtres de sécurité
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider daoAuthenticationProvider) throws Exception {
        http
                // Désactiver CSRF (typique pour les API REST stateless)
                .csrf(AbstractHttpConfigurer::disable)
                
                // Définir les règles d'autorisation
                .authorizeHttpRequests(auth -> auth
                        // Autoriser l'accès public (Auth et Swagger)
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        
                        // Sécuriser les endpoints CRUD (rôles)
                        .requestMatchers("/api/employes/**")
                            .hasAnyRole("ADMINISTRATEUR", "RESSOURCE_HUMAINE") 
                        
                        // Toutes les autres requêtes doivent être authentifiées par défaut
                        .anyRequest().authenticated()
                )
                
                // Gérer les sessions comme sans état (stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Enregistrer explicitement le provider d'authentification (DAO)
                .authenticationProvider(daoAuthenticationProvider)

                // Ajouter le filtre JWT AVANT le filtre standard de Spring Security
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}