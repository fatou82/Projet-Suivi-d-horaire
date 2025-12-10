package com.fatou82.suivi.suivihoraireapi.repositories;

import com.fatou82.suivi.suivihoraireapi.entities.Poste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PosteRepository extends JpaRepository<Poste, Long> {
    
    // Utile pour trouver un poste par son nom (par exemple, lors de la création via un formulaire)
    Optional<Poste> findByNom(String nom);
    
    // Utile pour vérifier l'unicité des abréviations
    boolean existsByAbreviation(String abreviation);
}