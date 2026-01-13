package com.fatou82.suivi.suivihoraireapi.repositories;

import com.fatou82.suivi.suivihoraireapi.entities.Pointage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PointageRepository extends JpaRepository<Pointage, Long> {

    // Récupérer les pointages d'un employé pour une journée spécifique
    @Query("SELECT p FROM Pointage p WHERE p.employe.id = :employeId " +
            "AND p.heurePointage >= :start AND p.heurePointage <= :end")
    List<Pointage> findByEmployeAndDateRange(
            @Param("employeId") Long employeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Trouver le dernier pointage de l'employé (pour savoir quel bouton afficher)
    Pointage findTopByEmployeIdOrderByHeurePointageDesc(Long employeId);

    List<Pointage> findByEmployeIdOrderByHeurePointageDesc(Long employeId);
}