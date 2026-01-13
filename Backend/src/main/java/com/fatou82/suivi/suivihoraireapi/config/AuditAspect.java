package com.fatou82.suivi.suivihoraireapi.config;

import com.fatou82.suivi.suivihoraireapi.annotations.LogAction;
import com.fatou82.suivi.suivihoraireapi.entities.Pointage;
import com.fatou82.suivi.suivihoraireapi.enums.PointageType;
import com.fatou82.suivi.suivihoraireapi.services.AuditLogService;
import com.fatou82.suivi.suivihoraireapi.entities.Employe; // Assurez-vous d'importer votre entité
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors; // Import nécessaire pour transformer la liste

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;

    @AfterReturning(pointcut = "@annotation(logAction)", returning = "result")
    public void logAfter(JoinPoint joinPoint, LogAction logAction, Object result) {
        Long idCible = null;
        String details = "";

        try {
            // CAS SPECIAL : UPDATE_CONFIGS (result est null car la méthode est void)
            if ("UPDATE_CONFIGS".equals(logAction.actionType())) {
                Object[] args = joinPoint.getArgs();
                if (args.length > 0 && args[0] instanceof List) {
                    List<?> configs = (List<?>) args[0];
                    details = String.format("[BULK UPDATE] %d paramètres système mis à jour.", configs.size());
                }
            }

            // CAS SPECIAL : Pointage (Entrée/Sortie)
            else
            if (result instanceof Pointage) {
                Pointage p = (Pointage) result;
                String heureFmt = p.getHeurePointage().toLocalTime().toString().substring(0, 5); // Ex: "17:05"

                if (p.getTypePointage().equals(PointageType.SORTIE)) {
                    // Affichage combiné : Heure de sortie + Durée calculée
                    details = String.format("[SORTIE] Pointage à %s | Durée totale travaillée : %.2f h",
                            heureFmt, p.getDureeTotale());
                } else {
                    details = String.format("[%s] Pointage enregistré à %s",
                            p.getTypePointage(), heureFmt);
                }
            }

            // CAS GENERAL : result n'est pas null (Employe, Poste, etc.)
            else if (result != null) {
                try {
                    idCible = (Long) result.getClass().getMethod("getId").invoke(result);
                } catch (Exception e) { /* Pas d'ID ? Pas grave */ }

                if (result instanceof Employe) {
                    Employe emp = (Employe) result;
                    String tousLesRoles = emp.getRoles().stream()
                            .map(role -> role.getNom().toString())
                            .collect(Collectors.joining(", "));

                    details = String.format("[%s] ID: %d | Matricule: %s | Nom: %s %s | Rôles: %s",
                            logAction.actionType().contains("UPDATE") ? "MODIFICATION" : "CREATION",
                            idCible, emp.getMatricule(), emp.getPrenom(), emp.getNom(), tousLesRoles);
                } else {
                    details = "Cible: " + logAction.entite() + " | Détails: " + result.toString();
                }
            }
        } catch (Exception e) {
            details = "Erreur lors du log de l'action: " + logAction.actionType();
        }

        // Sécurité : Si après tout ça details est encore vide, on met un message par défaut
        if (details.isEmpty()) {
            details = "Action effectuée sur " + logAction.entite();
        }

        auditLogService.logAdminAction(
                logAction.actionType(),
                logAction.entite(),
                idCible,
                details
        );
    }
}