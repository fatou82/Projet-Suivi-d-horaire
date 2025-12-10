package com.fatou82.suivi.suivihoraireapi.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data // Génère Getters, Setters, toString, equals/hashCode
public class EmployeDTO {
    
    // Champs de base
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String matricule;
    private LocalDate dateEmbauche;
    private Integer soldeConge;
    private String adresse;
    private Boolean actif;
    
    // 📢 CHAMPS MANQUANTS : Ajoutés pour résoudre l'erreur MapStruct
    private String motDePasse; // Nécessaire dans toEntity, ignoré dans toDto
    private String posteNom; // Mappé depuis Poste.nom
    private List<String> roles; // Mappé depuis Set<Role>
    
    // Les autres listes (demandesConge, absences, etc.) seront ajoutées plus tard
}