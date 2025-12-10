package com.fatou82.suivi.suivihoraireapi.services;

import com.fatou82.suivi.suivihoraireapi.entities.Employe;
import com.fatou82.suivi.suivihoraireapi.entities.Role;
import com.fatou82.suivi.suivihoraireapi.enums.RoleType;
import com.fatou82.suivi.suivihoraireapi.repositories.EmployeRepository;
import com.fatou82.suivi.suivihoraireapi.repositories.RoleRepository;
import com.fatou82.suivi.suivihoraireapi.repositories.PosteRepository;
import com.fatou82.suivi.suivihoraireapi.exceptions.ResourceNotFoundException; // 📢 Import nécessaire
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final RoleRepository roleRepository;
    private final PosteRepository posteRepository;
    private final PasswordEncoder passwordEncoder; // Injecté depuis SecurityConfig
    /**
     * Assigne le préfixe du rôle (ex: ADMINISTRATEUR -> AD).
     */
    private String getRolePrefix(Set<Role> roles) {
        // On prend le rôle le plus "élevé" ou simplement le premier si plusieurs sont assignés
        RoleType highestRole = roles.stream()
                .map(Role::getNom)
                .findFirst()
                .orElse(RoleType.EMPLOYE);

        return switch (highestRole) {
            case ADMINISTRATEUR -> "AD";
            case RESSOURCE_HUMAINE -> "RH";
            case MANAGER -> "MA";
            case EMPLOYE -> "EP";
        };
    }

    /**
     * Logique de génération du matricule : [ROLE]-[POSTE]-[NUM]
     */
    private String generateMatricule(Employe employe) {
        // 1. Définir l'abréviation du poste (lue de l'entité Poste)
        String posteAbrev = employe.getPoste().getAbreviation().toUpperCase();

        // 2. Définir le préfixe du rôle
        String rolePrefix = getRolePrefix(employe.getRoles());

        // 3. Trouver le numéro séquentiel
        Optional<Employe> lastEmploye = employeRepository.findTopByPoste_IdOrderByIdDesc(employe.getPoste().getId());
        int sequentialNumber = 1;

        if (lastEmploye.isPresent() && lastEmploye.get().getMatricule() != null) {
            String lastMatricule = lastEmploye.get().getMatricule();
            // On extrait la partie numérique après le dernier tiret '-'
            try {
                String numPart = lastMatricule.substring(lastMatricule.lastIndexOf('-') + 1);
                sequentialNumber = Integer.parseInt(numPart) + 1;
            } catch (Exception ignored) {
                // En cas d'erreur de parsing, on recommence à 1
            }
        }

        // Formatage du numéro sur 4 chiffres (ex: 1 -> "0001")
        String numPadded = String.format("%04d", sequentialNumber);

        // 4. Construction du matricule final
        return rolePrefix + "-" + posteAbrev + "-" + numPadded;
    }

    // =========================================================================
    // MÉTHODES CRUD PRINCIPALES
    // =========================================================================

    /**
     * Méthode principale de création et d'initialisation d'un nouvel employé.
     */
    public Employe createNewEmploye(Employe newEmploye) {
        
        // Assigner le rôle par défaut si non fourni (généralement EMPLOYE)
        if (newEmploye.getRoles() == null || newEmploye.getRoles().isEmpty()) {
            Role defaultRole = roleRepository.findByNom(RoleType.EMPLOYE)
                    .orElseThrow(() -> new RuntimeException("Rôle EMPLOYE par défaut non trouvé."));
            newEmploye.setRoles(Collections.singleton(defaultRole));
        }

        // 1. Hacher le mot de passe pour la sécurité
        newEmploye.setMotDePasse(passwordEncoder.encode(newEmploye.getMotDePasse()));

        // 2. Initialiser les champs obligatoires et par défaut
        if (newEmploye.getDateEmbauche() == null) {
            newEmploye.setDateEmbauche(LocalDate.now());
        }
        newEmploye.setActif(true);
        if (newEmploye.getSoldeConge() == null) {
            newEmploye.setSoldeConge(25); // 25 jours par défaut
        }

        // 3. Générer le matricule
        String matricule = generateMatricule(newEmploye);
        newEmploye.setMatricule(matricule);

        // 4. Enregistrer l'employé
        return employeRepository.save(newEmploye);
    }

    /**
     * Crée un nouvel employé à partir d'un DTO d'enregistrement (nom du poste donné).
     */
    public Employe createNewEmployeFromRegister(com.fatou82.suivi.suivihoraireapi.dto.RegisterRequest req) {
        Employe e = new Employe();
        e.setNom(req.getNom());
        e.setPrenom(req.getPrenom());
        e.setEmail(req.getEmail());
        e.setMotDePasse(req.getMotDePasse());
        e.setAdresse(req.getAdresse());

        // Parse dateEmbauche (supporte dd/MM/yyyy et ISO yyyy-MM-dd)
        if (req.getDateEmbauche() != null && !req.getDateEmbauche().isBlank()) {
            java.time.LocalDate parsed = null;
            try {
                parsed = java.time.LocalDate.parse(req.getDateEmbauche(), java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ex) {
                try {
                    parsed = java.time.LocalDate.parse(req.getDateEmbauche());
                } catch (Exception ignored) {}
            }
            if (parsed != null) e.setDateEmbauche(parsed);
        }

        // Resolve poste by name
        if (req.getPoste() != null && !req.getPoste().isBlank()) {
            PosteRepository pr = this.posteRepository;
            com.fatou82.suivi.suivihoraireapi.entities.Poste p = pr.findByNom(req.getPoste())
                    .orElseThrow(() -> new RuntimeException("Poste introuvable: " + req.getPoste()));
            e.setPoste(p);
        } else {
            throw new RuntimeException("Le poste est requis pour l'inscription.");
        }

        return createNewEmploye(e);
    }

    // =========================================================================
    // MÉTHODES UTILISÉES PAR LE CONTRÔLEUR D'AUTHENTIFICATION
    // =========================================================================

    /**
     * Trouve un employé par son email (utilisé par AuthService/Spring Security).
     * 📢 NÉCESSAIRE POUR AuthController.login
     */
    public Employe findByEmail(String email) {
        return employeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", "email", email));
    }

    /**
     * Crée le tout premier administrateur lors du démarrage (bootstrap).
     * 📢 NÉCESSAIRE POUR AuthController.registerAdmin
     */
    public Employe createInitialAdmin(Employe adminEmploye) {
        // 1. Récupérer le rôle ADMINISTRATEUR
        Role adminRole = roleRepository.findByNom(RoleType.ADMINISTRATEUR)
                .orElseThrow(() -> new RuntimeException("Le rôle ADMINISTRATEUR est introuvable. Veuillez vérifier les données initiales."));

        // 2. Assigner uniquement le rôle ADMINISTRATEUR
        adminEmploye.setRoles(Collections.singleton(adminRole));

        // 3. Utiliser la logique d'enregistrement existante (hachage et matricule)
        return createNewEmploye(adminEmploye);
    }
}