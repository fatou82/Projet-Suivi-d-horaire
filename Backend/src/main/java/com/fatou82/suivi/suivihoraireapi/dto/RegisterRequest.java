package com.fatou82.suivi.suivihoraireapi.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String adresse;
    // Date attendu en format dd/MM/yyyy ou ISO (yyyy-MM-dd)
    private String dateEmbauche;
    // Nom du poste (ex: "Comptable")
    private String poste;
}
