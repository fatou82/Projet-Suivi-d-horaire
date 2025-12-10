package com.fatou82.suivi.suivihoraireapi.controllers;

import com.fatou82.suivi.suivihoraireapi.dto.AuthRequest;
import com.fatou82.suivi.suivihoraireapi.dto.AuthResponse;
import com.fatou82.suivi.suivihoraireapi.entities.Employe;
import com.fatou82.suivi.suivihoraireapi.services.EmployeService;
import com.fatou82.suivi.suivihoraireapi.utils.JwtUtil;
import com.fatou82.suivi.suivihoraireapi.mapper.EmployeMapper;
import com.fatou82.suivi.suivihoraireapi.dto.EmployeDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

// 📢 Imports pour Swagger/OpenAPI (SpringDoc)
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth") 
@RequiredArgsConstructor
// 📢 Annotation Swagger pour grouper les endpoints
@Tag(name = "Authentification", description = "Gestion de la connexion (login) et de l'enregistrement initial de l'administrateur.")
public class AuthController {

    private final AuthenticationManager authenticationManager; 
    private final JwtUtil jwtUtil;
    private final EmployeService employeService;
    private final EmployeMapper employeMapper;
    
    /**
     * Endpoint de connexion
     */
    @Operation(summary = "Connexion de l'utilisateur", 
               description = "Authentifie un employé par email/mot de passe et retourne un jeton JWT.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Connexion réussie, retourne le JWT et les infos de l'utilisateur.",
                                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                   @ApiResponse(responseCode = "401", description = "Identifiants invalides ou utilisateur non trouvé.")
               })
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody AuthRequest authenticationRequest) {
        
        try {
            // 1. Authentification via Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getMotDePasse())
            );
            
            // 2. Récupération des détails de l'utilisateur après l'authentification réussie
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // 3. Extraction des rôles pour le JWT
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // 4. Création du JWT
            final String jwt = jwtUtil.generateToken(userDetails, roles);

            // 5. Récupérer l'entité Employe complète pour obtenir le matricule
            Employe employe = employeService.findByEmail(userDetails.getUsername());

            // 6. Retourner la réponse
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwt)
                    .matricule(employe.getMatricule())
                    .email(employe.getEmail())
                    .roles(roles)
                    .build());

        } catch (BadCredentialsException e) {
            // Mauvais identifiants
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Email ou mot de passe invalide."));
        } catch (AuthenticationException e) {
            // Autres erreurs d'authentification
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Erreur d'authentification."));
        }
    }
    
    /**
     * Endpoint d'enregistrement public d'un employé
     */
    @Operation(summary = "Enregistrement d'un nouvel employé",
               description = "Crée un compte employé. Le rôle par défaut (EMPLOYE) sera attribué si non fourni.")
    @ApiResponse(responseCode = "201", description = "Employé créé avec succès")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody com.fatou82.suivi.suivihoraireapi.dto.RegisterRequest registerRequest) {
        try {
            com.fatou82.suivi.suivihoraireapi.entities.Employe created = employeService.createNewEmployeFromRegister(registerRequest);
            EmployeDTO dto = employeMapper.toDto(created);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}