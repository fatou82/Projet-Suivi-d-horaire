package com.fatou82.suivi.suivihoraireapi.services;

import com.fatou82.suivi.suivihoraireapi.annotations.LogAction;
import com.fatou82.suivi.suivihoraireapi.entities.Employe;
import com.fatou82.suivi.suivihoraireapi.entities.RegleConfiguration;
import com.fatou82.suivi.suivihoraireapi.repositories.EmployeRepository;
import com.fatou82.suivi.suivihoraireapi.repositories.RegleConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConfigurationService {
    @Autowired
    private RegleConfigurationRepository repo;
    @Autowired
    private EmployeRepository employeRepository;

    // Récupérer tous les paramètres de configuration
    public List<RegleConfiguration> getAllConfigs() {
        return repo.findAll();
    }

    // Mettre à jour plusieurs configurations en une seule fois
    @LogAction(actionType = "UPDATE_CONFIGS", entite = "RegleConfiguration")
    public void updateConfigs(List<RegleConfiguration> configs, boolean applyToAll) {
        for (RegleConfiguration nouvelleConfig : configs) {
            repo.findByNomCle(nouvelleConfig.getNomCle()).ifPresent(existante -> {
                nouvelleConfig.setId(existante.getId());
            });

            // Si la clé est le solde et que l'utilisateur a dit "OUI"
            if (applyToAll && "SOLDE_CONGE_INITIAL".equals(nouvelleConfig.getNomCle())) {
                int nouveauSolde = Integer.parseInt(nouvelleConfig.getValeur());
                updateAllEmployeesLeaveBalance(nouveauSolde);
            }
        }
        repo.saveAll(configs);
    }

    private void updateAllEmployeesLeaveBalance(int nouveauSolde) {
        List<Employe> employes = employeRepository.findAll();
        for (Employe e : employes) {
            if (Boolean.TRUE.equals(e.getActif())) {
                e.setSoldeConge(nouveauSolde);
            }
        }
        employeRepository.saveAll(employes);
    }

    // Trouver une configuration par sa clé
    @LogAction(actionType = "FIND_CONFIG_BY_KEY", entite = "RegleConfiguration")
    public Optional<RegleConfiguration> findByKey(String nomCle) {
        return repo.findByNomCle(nomCle);
    }
}
