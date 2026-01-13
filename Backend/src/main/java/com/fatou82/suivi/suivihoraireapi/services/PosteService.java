package com.fatou82.suivi.suivihoraireapi.services;

import com.fatou82.suivi.suivihoraireapi.annotations.LogAction;
import com.fatou82.suivi.suivihoraireapi.entities.Poste;
import com.fatou82.suivi.suivihoraireapi.repositories.PosteRepository;
import com.fatou82.suivi.suivihoraireapi.repositories.EmployeRepository;
import com.fatou82.suivi.suivihoraireapi.services.AuditLogService;
import com.fatou82.suivi.suivihoraireapi.exceptions.ResourceNotFoundException;
import com.fatou82.suivi.suivihoraireapi.dto.PosteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PosteService {

    private final PosteRepository posteRepository;
    private final EmployeRepository employeRepository;
    private final AuditLogService auditLogService;

    /**
     * Crée un nouveau poste après vérification de l'unicité.
     */
    @LogAction(actionType = "CREATE_POSTE", entite = "Poste")
    public Poste createPoste(PosteDTO posteDTO) {
        
        // 1. Vérification de l'unicité du Nom
        if (posteRepository.findByNom(posteDTO.getNom()).isPresent()) {
            throw new IllegalArgumentException("Un poste avec le nom '" + posteDTO.getNom() + "' existe déjà.");
        }

        // 2. Vérification de l'unicité de l'Abréviation
        String abreviationUpper = posteDTO.getAbreviation().toUpperCase();
        if (posteRepository.findByAbreviation(abreviationUpper).isPresent()) {
            throw new IllegalArgumentException("L'abréviation '" + abreviationUpper + "' est déjà utilisée.");
        }
        
        Poste newPoste = new Poste();
        newPoste.setNom(posteDTO.getNom());
        newPoste.setAbreviation(abreviationUpper);
        newPoste.setDescription(posteDTO.getDescription());
        
        return posteRepository.save(newPoste);
    }

    /**
     * Récupère tous les postes.
     */
    public List<Poste> findAllPostes() {
        return posteRepository.findAll();
    }

    /**
     * Récupère un poste par son ID.
     */
    public Poste findPosteById(Long id) {
        return posteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Poste", "id", id.toString()));
    }

    /**
     * Met à jour un poste existant.
     */
    @LogAction(actionType = "UPDATE_POSTE", entite = "Poste")
    public Poste updatePoste(Long id, PosteDTO posteDTO) {
        Poste existingPoste = findPosteById(id);

        // 1. Vérification des unicité (Nom et Abréviation)
        
        // Vérification du Nom (si le nom a changé)
        Optional<Poste> posteWithSameName = posteRepository.findByNom(posteDTO.getNom());
        if (posteWithSameName.isPresent() && !posteWithSameName.get().getId().equals(id)) {
            throw new IllegalArgumentException("Un autre poste porte déjà le nom '" + posteDTO.getNom() + "'.");
        }

        // Vérification de l'Abréviation (si l'abréviation a changé)
        String abreviationUpper = posteDTO.getAbreviation().toUpperCase();
        Optional<Poste> posteWithSameAbrev = posteRepository.findByAbreviation(abreviationUpper);
        if (posteWithSameAbrev.isPresent() && !posteWithSameAbrev.get().getId().equals(id)) {
            throw new IllegalArgumentException("L'abréviation '" + abreviationUpper + "' est déjà utilisée par un autre poste.");
        }

        // 2. Mise à jour des champs
        existingPoste.setNom(posteDTO.getNom());
        existingPoste.setAbreviation(abreviationUpper);
        existingPoste.setDescription(posteDTO.getDescription());
        
        return posteRepository.save(existingPoste);
    }

    /**
     * Supprime un poste par son ID.
     * NOTE: Une vérification devrait être ajoutée pour s'assurer qu'aucun employé n'est rattaché à ce poste.
     */
    @LogAction(actionType = "DELETE_POSTE", entite = "Poste")
    public void deletePoste(Long id) {
        Poste posteToDelete = findPosteById(id);
        
        // Vérification des employés rattachés à ce poste
        long employeCount = employeRepository.countByPosteId(id);
        
        if (employeCount > 0) {
            throw new IllegalArgumentException(
                "Impossible de supprimer le poste '" + posteToDelete.getNom() + 
                "' car " + employeCount + " employé(s) y sont rattachés."
            );
        }
        
        posteRepository.delete(posteToDelete);
    }
}