package com.fatou82.suivi.suivihoraireapi.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String token; // Le JSON Web Token à stocker côté client
    private String matricule;
    private String email;
    private List<String> roles; // Les rôles pour gérer les droits dans l'interface Angular
}