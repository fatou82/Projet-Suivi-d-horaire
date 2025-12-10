package com.fatou82.suivi.suivihoraireapi.repositories;

import com.fatou82.suivi.suivihoraireapi.entities.Employe;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
// JpaRepository fournit automatiquement save(), findAll(), findById(), etc.
public interface EmployeRepository extends JpaRepository<Employe, Long> {
    
    // Méthode standard pour l'authentification
    Optional<Employe> findByEmail(String email);
    
    // Trouve le dernier employé pour un poste donné (méthode dérivée Spring Data JPA)
    Optional<Employe> findTopByPoste_IdOrderByIdDesc(Long posteId);

    // Vérifie s'il existe au moins un employé possédant le rôle donné
    boolean existsByRolesNom(com.fatou82.suivi.suivihoraireapi.enums.RoleType nom);
}