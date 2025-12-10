package com.fatou82.suivi.suivihoraireapi.repositories;

import com.fatou82.suivi.suivihoraireapi.entities.Role;
import com.fatou82.suivi.suivihoraireapi.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    // Méthode pour retrouver un Rôle par son Enum, nécessaire pour l'assignation par défaut.
    Optional<Role> findByNom(RoleType nom);
}