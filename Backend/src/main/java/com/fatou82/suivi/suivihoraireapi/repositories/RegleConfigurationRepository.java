package com.fatou82.suivi.suivihoraireapi.repositories;

import com.fatou82.suivi.suivihoraireapi.entities.RegleConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegleConfigurationRepository extends JpaRepository<RegleConfiguration, Long> {
    Optional<RegleConfiguration> findByNomCle(String nomCle);
}
