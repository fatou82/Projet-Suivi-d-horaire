package com.fatou82.suivi.suivihoraireapi.controllers;

import com.fatou82.suivi.suivihoraireapi.entities.RegleConfiguration;
import com.fatou82.suivi.suivihoraireapi.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/configurations")
public class ConfigurationController {

    @Autowired
    private ConfigurationService configurationService;

    // Récupérer tous les paramètres pour les afficher dans le formulaire Admin
    @GetMapping
    public ResponseEntity<List<RegleConfiguration>> getAllConfigs() {
        return ResponseEntity.ok(configurationService.getAllConfigs());
    }

    // Mettre à jour plusieurs règles en une seule fois, bulk : en masse, groupé
    @PutMapping("/bulk")
    public ResponseEntity<Void> updateAllConfigs(
            @RequestBody List<RegleConfiguration> configs,
            @RequestParam(defaultValue = "false") boolean applyToAll) { // Ajoute ce paramètre

        configurationService.updateConfigs(configs, applyToAll);
        return ResponseEntity.noContent().build();
    }

    // Récupérer une règle spécifique par sa clé (utile pour le moteur de calcul des retards)
    @GetMapping("/key/{nomCle}")
    public ResponseEntity<RegleConfiguration> getConfigByKey(@PathVariable String nomCle) {
        return configurationService.findByKey(nomCle)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
