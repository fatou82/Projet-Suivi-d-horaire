package com.fatou82.suivi.suivihoraireapi.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.fatou82.suivi.suivihoraireapi.dto.PosteDTO;
import com.fatou82.suivi.suivihoraireapi.entities.Poste;
import com.fatou82.suivi.suivihoraireapi.services.PosteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

// Annotations Swagger/OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/postes")
@RequiredArgsConstructor
@Tag(name = "Gestion des Postes", description = "API pour les opérations CRUD sur les postes (rôles fonctionnels des employés).") // 🚨 NOUVEAU TAG
public class PosteController {

    private final PosteService posteService;

    // POST /api/postes
    @PostMapping
    @Operation(summary = "Créer un nouveau poste", 
               description = "Ajoute un nouveau poste. Le nom et l'abréviation doivent être uniques.")
    public ResponseEntity<Poste> createPoste(@Valid @RequestBody PosteDTO posteDTO) {
        Poste newPoste = posteService.createPoste(posteDTO);
        return new ResponseEntity<>(newPoste, HttpStatus.CREATED);
    }

    // GET /api/postes
    @GetMapping
    @Operation(summary = "Récupérer la liste de tous les postes")
    public ResponseEntity<List<Poste>> getAllPostes() {
        List<Poste> postes = posteService.findAllPostes();
        return ResponseEntity.ok(postes);
    }

    // GET /api/postes/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un poste par son ID")
    public ResponseEntity<Poste> getPosteById(@PathVariable Long id) {
        Poste poste = posteService.findPosteById(id);
        return ResponseEntity.ok(poste);
    }

    // PUT /api/postes/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un poste existant", 
               description = "Met à jour le nom, l'abréviation et la description d'un poste.")
    public ResponseEntity<Poste> updatePoste(@PathVariable Long id, @Valid @RequestBody PosteDTO posteDTO) {
        Poste updatedPoste = posteService.updatePoste(id, posteDTO);
        return ResponseEntity.ok(updatedPoste);
    }

    // DELETE /api/postes/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un poste", 
               description = "Supprime un poste par son ID. Impossible si des employés y sont rattachés.")
    public ResponseEntity<Void> deletePoste(@PathVariable Long id) {
        posteService.deletePoste(id);
        return ResponseEntity.noContent().build();
    }
}