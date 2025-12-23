package com.fatou82.suivi.suivihoraireapi.services;

import com.fatou82.suivi.suivihoraireapi.entities.Employe;
import com.fatou82.suivi.suivihoraireapi.entities.AuditLog;
import com.fatou82.suivi.suivihoraireapi.repositories.AuditLogRepository;
import com.fatou82.suivi.suivihoraireapi.repositories.EmployeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor // Gère l'injection par constructeur des champs 'final'
public class AuditLogService {

    private final EmployeRepository employeRepository;
    // **2. AJOUT DU REPOSITORY POUR SAUVEGARDER LES LOGS**
    private final AuditLogRepository auditLogRepository; 

    public void logAdminAction(String actionType, String entiteCible, Long entiteCibleId, String details) {
        // 1. Récupérer l'utilisateur connecté (Admin/RH)
        // NOTE: Ceci suppose que l'utilisateur est bien l'employé (login par email)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // 2. Trouver l'Employé (l'acteur)
        Employe acteur = employeRepository.findByEmail(username) 
            .orElseThrow(() -> new RuntimeException("Utilisateur d'audit non trouvé: " + username));

        // 3. Construire et sauvegarder le log
        AuditLog log = new AuditLog();
        log.setDateAction(LocalDateTime.now());
        log.setActionType(actionType); 
        log.setEntiteCible(entiteCible);
        log.setEntiteCibleId(entiteCibleId);
        log.setDetails(details); // Stocke les données du changement
        log.setEmploye(acteur); // L'Admin/RH qui a fait l'action
        
        auditLogRepository.save(log);
    }
}