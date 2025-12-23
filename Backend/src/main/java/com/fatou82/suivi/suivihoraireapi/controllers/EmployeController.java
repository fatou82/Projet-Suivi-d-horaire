package com.fatou82.suivi.suivihoraireapi.controllers;

import com.fatou82.suivi.suivihoraireapi.dto.EmployeDTO;
import com.fatou82.suivi.suivihoraireapi.dto.RegisterRequest;
import com.fatou82.suivi.suivihoraireapi.dto.UpdateRolesRequest;
import com.fatou82.suivi.suivihoraireapi.services.EmployeService;
import com.fatou82.suivi.suivihoraireapi.mapper.EmployeMapper;
import com.fatou82.suivi.suivihoraireapi.entities.Employe; // Import nécessaire
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

// Import Swagger/OpenAPI (SpringDoc)
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/employes") // Le chemin de base pour ce contrôleur
@RequiredArgsConstructor
@Tag(name = "Gestion des Employés (Sécurisé)", description = "Opérations CRUD sur les employés.")
@SecurityRequirement(name = "Bearer Authentication") // Indique la sécurité via JWT
public class EmployeController {

    private final EmployeService employeService;
    private final EmployeMapper employeMapper;

    /**
     * Endpoint: GET /api/employes
     * Permet la création d'un employé.
     */

    @Operation(summary = "Création d'un nouvel employé",
               description = "Ajoute un employé avec un rôle spécifique. Nécessite le rôle ADMINISTRATEUR ou RESSOURCE_HUMAINE.")
    @PostMapping 
    @ResponseStatus(HttpStatus.CREATED) // Retourne le statut 201
    public ResponseEntity<EmployeDTO> createEmploye(@RequestBody RegisterRequest req) {
        
        Employe newEmploye = employeService.createNewEmployeFromRegister(req);
        EmployeDTO employeDTO = employeMapper.toDto(newEmploye);

        return ResponseEntity.status(HttpStatus.CREATED).body(employeDTO);
    }

     /**
     * Endpoint: GET /api/employes
     * Retourne la liste de tous les employés (au format DTO).
     */

    @Operation(summary = "Lister tous les employés",
               description = "Récupère la liste de tous les employés enregistrés. Nécessite le rôle ADMINISTRATEUR ou RESSOURCE_HUMAINE.")
    @GetMapping
    public ResponseEntity<List<EmployeDTO>> findAll() {
        
        // 1. Appelle le Service pour récupérer la liste des Entités Employe
        List<Employe> employes = employeService.findAllEmployes();

        // 2. Utilise le Mapper pour convertir la List<Employe> en List<EmployeDTO>
        List<EmployeDTO> employeDTOs = employeMapper.toDtoList(employes);

        // 3. Retourne la liste des DTOs avec un statut HTTP 200 (OK) sortie un tableau JSON 
        return ResponseEntity.ok(employeDTOs);
    }

    /**
     * Endpoint: PUT /api/employes/{id}
     * Met à jour les informations d'un employé.
     */

    @Operation(summary = "Mise à jour des informations d'un employé",
           description = "Mets à jours le nom, prénom, email, mot de passe et l'adresse de l'employé. **Le rôle doit être modifié via l'endpoint PATCH /role.** Nécessite le rôle ADMINISTRATEUR ou RESSOURCE_HUMAINE.")
    @PutMapping("/{id}") // Utilise un chemin variable pour l'ID
    public ResponseEntity<EmployeDTO> updateEmploye(
            @PathVariable Long id, 
            @RequestBody RegisterRequest updateReq) {
        
        Employe updatedEmploye = employeService.updateEmploye(id, updateReq);
        EmployeDTO employeDTO = employeMapper.toDto(updatedEmploye);
        
        // 200 OK est standard pour un PUT réussi
        return ResponseEntity.ok(employeDTO);
    }

    /**
     * Endpoint: PUT /api/employes/me
     * Permet à l'employé connecté de mettre à jour SES PROPRES informations personnelles.
     */

    @Operation(summary = "Auto-modification de profil",
               description = "Mise à jour du nom, prénom, email et adresse de l'utilisateur connecté.")
    @PutMapping("/me")
    public ResponseEntity<EmployeDTO> updateSelf(
            Principal principal, 
            @RequestBody RegisterRequest updateReq) {
        
        Employe updatedEmploye = employeService.updateSelf(principal.getName(), updateReq);
        EmployeDTO employeDTO = employeMapper.toDto(updatedEmploye);
        
        return ResponseEntity.ok(employeDTO);
    }
    
    /**
     * Endpoint: PATCH /api/employes/{id}/role
     * Met à jour le rôle d'un employé spécifique.
     */

    @Operation(summary = "Mise à jour ou ajout des rôles d'un employé",
               description = "Modifie ou ajoute les rôles d'un employé. Nécessite le rôle ADMINISTRATEUR.")
    @PatchMapping("/{id}/role") // PATCH est souvent utilisé pour des mises à jour partielles
    public ResponseEntity<EmployeDTO> updateRole(
        @PathVariable Long id, 
        @RequestBody UpdateRolesRequest roleRequest) { // Utiliser le DTO List
        
        // Le service reçoit maintenant une liste de noms de rôles
        Employe updatedEmploye = employeService.updateEmployeRoles(id, roleRequest.getRoleNames()); 
        EmployeDTO employeDTO = employeMapper.toDto(updatedEmploye);
        
        return ResponseEntity.ok(employeDTO);
    }
   
    /**
     * Endpoint: PATCH /api/employes/{id}/password
     * Permet à l'Admin/RH de réinitialiser le mot de passe d'un autre employé.
     */
    @Operation(summary = "Réinitialiser le mot de passe d'un employé",
               description = "Définit un nouveau mot de passe pour l'employé ciblé. Nécessite le rôle ADMINISTRATEUR ou RESSOURCE_HUMAINE.")
    @PatchMapping("/{id}/password")
    public ResponseEntity<EmployeDTO> resetPassword(
            @PathVariable Long id, 
            @RequestBody Map<String, String> passwordRequest) {
        
        String newPassword = passwordRequest.get("newPassword");
        
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        Employe updatedEmploye = employeService.updatePassword(id, newPassword);
        EmployeDTO employeDTO = employeMapper.toDto(updatedEmploye);
        
        return ResponseEntity.ok(employeDTO);
    }
    /**
     * Endpoint: DELETE /api/employes/{id}
     * Désactive logiquement (soft delete) un employé.
     */

     @Operation(summary = "Suppression d'un employé",
               description = "Récupère l'id et supprime l'employé. Nécessite le rôle ADMINISTRATEUR ou RESSOURCE_HUMAINE.")

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmploye(@PathVariable Long id) {
        
        employeService.deactivateEmploye(id);
        
        // 204 No Content est standard pour une suppression/désactivation réussie
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint: PATCH /api/employes/{id}/reactivate
     * Réactive logiquement un employé.
     */
    @Operation(summary = "Réactivation d'un employé",
               description = "Réactive un employé précédemment désactivé (actif = true). Nécessite le rôle ADMINISTRATEUR ou RESSOURCE_HUMAINE.")
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<EmployeDTO> reactivateEmploye(@PathVariable Long id) {
        
        Employe reactivatedEmploye = employeService.reactivateEmploye(id);
        EmployeDTO employeDTO = employeMapper.toDto(reactivatedEmploye);
        
        return ResponseEntity.ok(employeDTO);
    }

}