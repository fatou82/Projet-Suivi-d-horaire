package com.fatou82.suivi.suivihoraireapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PosteDTO {
    
    private Long id;

    @NotBlank(message = "Le nom du poste est obligatoire.")
    private String nom; 
    
    @NotBlank(message = "L'abréviation est obligatoire.")
    @Size(min = 3, max = 3, message = "L'abréviation doit contenir exactement 3 caractères.")
    private String abreviation;
    
    // Ajout d'une taille max par sécurité
    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères.")
    private String description;
}