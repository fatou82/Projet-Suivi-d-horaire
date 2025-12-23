package com.fatou82.suivi.suivihoraireapi.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ChangePasswordRequest {
    
    @NotBlank
    private String ancienMotDePasse;

    @NotBlank
    private String nouveauMotDePasse;
}