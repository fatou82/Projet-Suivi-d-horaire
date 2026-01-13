package com.fatou82.suivi.suivihoraireapi.services;

import com.fatou82.suivi.suivihoraireapi.entities.Pointage;
import com.fatou82.suivi.suivihoraireapi.entities.Employe;
import com.fatou82.suivi.suivihoraireapi.enums.PointageType;
import com.fatou82.suivi.suivihoraireapi.repositories.PointageRepository;
import com.fatou82.suivi.suivihoraireapi.annotations.LogAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PointageService {

    private final PointageRepository pointageRepository;
    private final ConfigurationService configService; // Pour récupérer tes seuils dynamiques

    // Effectuer un pointage (entrée ou sortie)
    @Transactional
    @LogAction(actionType = "POINTAGE", entite = "Pointage")
    public Pointage effectuerPointage(Employe employe, PointageType type) {
        LocalDateTime maintenant = LocalDateTime.now();

        // 2. Création de l'objet Pointage
        Pointage pointage = new Pointage();
        pointage.setEmploye(employe);
        pointage.setHeurePointage(maintenant);
        pointage.setTypePointage(type);
        pointage.setSource("WEB_INTERFACE");

        // verification retard
        if (type == PointageType.ENTREE) {
            verifierRetard(maintenant.toLocalTime());
        }
        // CALCUL DURÉE PAUSE
        else if (type == PointageType.FIN_PAUSE) {
            calculerDureePause(pointage, employe, maintenant);
        }
        // CALCUL DURÉE TRAVAIL
        else if (type == PointageType.SORTIE) {
            calculerDureeTravail(pointage, employe, maintenant);
        }

        return pointageRepository.save(pointage);
    }

    // Fonction pour verifier si un employé est en retard
    private void verifierRetard(LocalTime heureActuelle) {
        // Récupération dynamique du seuil configuré par l'admin
        String seuilRetardStr = configService.findByKey("SEUIL_RETARD")
                .map(c -> c.getValeur())
                .orElse("08:15"); // Valeur par défaut si non configurée

        LocalTime seuilRetard = LocalTime.parse(seuilRetardStr);

        if (heureActuelle.isAfter(seuilRetard)) {
            // Ici, tu pourrais envoyer une notification ou marquer une entité "Absence/Retard"
            System.out.println("Alerte : Retard détecté !");
        }
    }

    // Pour le calcul de la durré de travail
    private void calculerDureeTravail(Pointage pointageSortie, Employe employe, LocalDateTime heureSortie) {
        // On cherche le pointage d'entrée du jour (entre 00h00 et maintenant)
        LocalDateTime debutJour = heureSortie.toLocalDate().atStartOfDay();

        List<Pointage> pointagesDuJour = pointageRepository.findByEmployeAndDateRange(
                employe.getId(), debutJour, heureSortie
        );

        // On cherche le dernier pointage de type ENTREE
        pointagesDuJour.stream()
                .filter(p -> p.getTypePointage() == PointageType.ENTREE)
                .findFirst()
                .ifPresent(entree -> {
                    Duration duration = Duration.between(entree.getHeurePointage(), heureSortie);
                    // On transforme la durée en heures (ex: 8.5 pour 8h30)
                    float heuresTravaillees = duration.toMinutes() / 60.0f;
                    pointageSortie.setDureeTotale(heuresTravaillees);
                });
    }

    // Récupérer les pointages d'un employé dans une plage de dates
    public List<Pointage> getPointagesEmploye(Long employeId, LocalDateTime start, LocalDateTime end) {
        // Si ils sont null, le repository peut renvoyer "tout" (plus récent au moins récent)
        if (start != null && end != null) {
            return pointageRepository.findByEmployeAndDateRange(employeId, start, end);
        }
        return pointageRepository.findByEmployeIdOrderByHeurePointageDesc(employeId);
    }

    // Pour le calcul de la durré de pause
    private void calculerDureePause(Pointage pointageFinPause, Employe employe, LocalDateTime heureFin) {
        LocalDateTime debutJour = heureFin.toLocalDate().atStartOfDay();
        List<Pointage> pointagesDuJour = pointageRepository.findByEmployeAndDateRange(
                employe.getId(), debutJour, heureFin
        );

        // On cherche le dernier DEBUT_PAUSE
        pointagesDuJour.stream()
                .filter(p -> p.getTypePointage() == PointageType.DEBUT_PAUSE)
                .reduce((first, second) -> second) // On prend le plus récent
                .ifPresent(debut -> {
                    Duration duration = Duration.between(debut.getHeurePointage(), heureFin);
                    pointageFinPause.setDureeTotale(duration.toMinutes() / 60.0f);
                });
    }
}