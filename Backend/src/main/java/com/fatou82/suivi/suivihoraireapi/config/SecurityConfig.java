package com.fatou82.suivi.suivihoraireapi.config;

import com.fatou82.suivi.suivihoraireapi.config.JwtAuthFilter;
import com.fatou82.suivi.suivihoraireapi.services.AuthService;

// import io.swagger.v3.oas.models.PathItem.HttpMethod;
import org.springframework.http.HttpMethod;
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
                        
                      // 🎯 RÈGLES DÉTAILLÉES POUR /api/employes

                        // 1. CRÉATION (POST /api/employes) : Restreinte
                        // Seuls l'Admin et la RH peuvent créer de nouveaux employés.
                        .requestMatchers(HttpMethod.POST, "/api/employes")
                            .hasAnyRole("ADMINISTRATEUR", "RESSOURCE_HUMAINE") 

                        // 2. LISTE (GET /api/employes) : Restreinte
                        // Seuls l'Admin et la RH (et éventuellement le Manager) peuvent lister tous les employés.
                        .requestMatchers(HttpMethod.GET, "/api/employes")
                            .hasAnyRole("ADMINISTRATEUR", "RESSOURCE_HUMAINE") 
                            
                        // 3. MISE À JOUR DU RÔLE (PATCH /api/employes/{id}/role) : Très Restreinte
                        // Seul l'ADMINISTRATEUR peut changer le rôle d'un autre employé.
                        .requestMatchers(HttpMethod.PATCH, "/api/employes/{id}/role")
                            .hasRole("ADMINISTRATEUR")
                            
                        // 4. SUPPRESSION/DÉSACTIVATION (DELETE /api/employes/{id}) : Restreinte
                        .requestMatchers(HttpMethod.DELETE, "/api/employes/{id}")
                            .hasAnyRole("ADMINISTRATEUR", "RESSOURCE_HUMAINE")
                            
                        // 5. Permet à n'importe quel utilisateur connecté (ADMIN, MANAGER, EMPLOYE) de modifier son propre profil.
                        .requestMatchers(HttpMethod.PUT, "/api/employes/me").authenticated()

                        // 6. MISE À JOUR GÉNÉRALE (PUT /api/employes/{id}) : 
                        // Cette route sert uniquement à l'Admin/RH pour modifier les autres :
                        .requestMatchers(HttpMethod.PUT, "/api/employes/{id}")
                            .hasAnyRole("ADMINISTRATEUR", "RESSOURCE_HUMAINE")

                        // 7. RÉACTIVATION (PATCH /api/employes/{id}/reactivate) : Restreinte
                        .requestMatchers(HttpMethod.PATCH, "/api/employes/{id}/reactivate")
                            .hasAnyRole("ADMINISTRATEUR", "RESSOURCE_HUMAINE")

                        // 8. RÉINITIALISATION MOT DE PASSE (PATCH /api/employes/{id}/password) : Restreinte
                        .requestMatchers(HttpMethod.PATCH, "/api/employes/{id}/password")
                            .hasAnyRole("ADMINISTRATEUR")
                            
                        // 9. CHANGEMENT DE MOT DE PASSE (PATCH /api/auth/change-password) : Authentifié
                        // Nécessite d'être connecté (accessible à tous les rôles)
                        .requestMatchers(HttpMethod.PATCH, "/api/auth/change-password").authenticated()

                        // 🎯 NOUVELLES RÈGLES DÉTAILLÉES POUR /api/postes

                        // 1. CRÉATION (POST /api/postes) : Restreinte à Admin/RH
                        .requestMatchers(HttpMethod.POST, "/api/postes")
                            .hasAnyRole("ADMINISTRATEUR", "RESSOURCE_HUMAINE")

                        // 2. LISTE/CONSULTATION (GET /api/postes et /api/postes/{id}) : Restreinte à Admin/RH
                        .requestMatchers(HttpMethod.GET, "/api/postes", "/api/postes/{id}")
                            .hasAnyRole("ADMINISTRATEUR", "RESSOURCE_HUMAINE")

                        // 3. MISE À JOUR (PUT /api/postes/{id}) : Restreinte à Admin/RH
                        .requestMatchers(HttpMethod.PUT, "/api/postes/{id}")
                            .hasAnyRole("ADMINISTRATEUR", "RESSOURCE_HUMAINE")

                        // 4. SUPPRESSION (DELETE /api/postes/{id}) : Restreinte à Admin/RH
                        .requestMatchers(HttpMethod.DELETE, "/api/postes/{id}")
                            .hasAnyRole("ADMINISTRATEUR", "RESSOURCE_HUMAINE")

                         // Règle par défaut (Toutes les autres requêtes sur des chemins non spécifiés)
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