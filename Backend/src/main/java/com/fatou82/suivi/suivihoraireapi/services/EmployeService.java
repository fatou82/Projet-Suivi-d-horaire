package com.fatou82.suivi.suivihoraireapi.services;

import com.fatou82.suivi.suivihoraireapi.entities.Employe;
import com.fatou82.suivi.suivihoraireapi.entities.Role;
import com.fatou82.suivi.suivihoraireapi.enums.RoleType;
import com.fatou82.suivi.suivihoraireapi.repositories.EmployeRepository;
import com.fatou82.suivi.suivihoraireapi.repositories.RoleRepository;
import com.fatou82.suivi.suivihoraireapi.repositories.PosteRepository;
import com.fatou82.suivi.suivihoraireapi.services.AuditLogService;
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
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder; // Injecté depuis SecurityConfig
    /**
     * Assigne le préfixe du rôle (ex: ADMINISTRATEUR -> AD).
     */
    private String getRolePrefix(Set<Role> roles) {
        RoleType highestRole = RoleType.EMPLOYE; // Rôle par défaut le plus bas

        // 1. Définir l'ordre de priorité des rôles pour la sélection du matricule
        List<RoleType> priorityOrder = List.of(
            RoleType.ADMINISTRATEUR, 
            RoleType.RESSOURCE_HUMAINE, 
            RoleType.MANAGER, 
            RoleType.EMPLOYE
        );

        // 2. Parcourir les rôles de l'employé pour trouver le plus prioritaire
        for (Role role : roles) {
            int currentIndex = priorityOrder.indexOf(role.getNom());
            int highestIndex = priorityOrder.indexOf(highestRole);

            // Si le rôle actuel est plus prioritaire (a un index plus petit)
            if (currentIndex != -1 && currentIndex < highestIndex) {
                highestRole = role.getNom();
            }
        }

        // 3. Retourner le préfixe basé sur le rôle le plus prioritaire
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
            Set<Role> defaultRoles = new HashSet<>();
            defaultRoles.add(defaultRole);
            newEmploye.setRoles(defaultRoles);
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
        Employe savedEmploye = employeRepository.save(newEmploye); // 👈 Sauvegarder d'abord pour obtenir l'ID
        
        // 5. JOURNAL D'AUDIT ADMINISTRATIF : Création
        String rolesList = newEmploye.getRoles().stream()
            .map(r -> r.getNom().name()) // Assurez-vous d'avoir RoleType.name()
            .reduce((a, b) -> a + ", " + b)
            .orElse("AUCUN");

        String details = String.format("Création de l'employé %s %s. Matricule: %s. Rôles initiaux: %s.",
            savedEmploye.getPrenom(), savedEmploye.getNom(), savedEmploye.getMatricule(), rolesList);
        
        auditLogService.logAdminAction("CREATE_EMPLOYE", "Employe", savedEmploye.getId(), details);
        
        return savedEmploye;
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
            com.fatou82.suivi.suivihoraireapi.entities.Poste p = posteRepository.findByNom(req.getPoste())
                    .orElseThrow(() -> new RuntimeException("Poste introuvable: " + req.getPoste()));
            e.setPoste(p);
        } else {
            // Cette vérification est essentielle
            throw new RuntimeException("Le poste est requis pour l'inscription."); 
        }

        // Assignation du rôle si spécifié dans le DTO
        if (req.getRoleName() != null && !req.getRoleName().isBlank()) {
        try {
            RoleType specifiedRole = RoleType.valueOf(req.getRoleName().toUpperCase());
            Role role = roleRepository.findByNom(specifiedRole)
                    .orElseThrow(() -> new RuntimeException("Rôle spécifié introuvable: " + req.getRoleName()));
            
            Set<Role> rolesFromDto = new HashSet<>();
            rolesFromDto.add(role);
            e.setRoles(rolesFromDto);
            
        } catch (IllegalArgumentException ex) {
            // Le rôle fourni dans le DTO n'est pas une valeur valide de l'enum RoleType
            throw new RuntimeException("Rôle non valide: " + req.getRoleName());
        }
        } else {
            // Si aucun rôle n'est spécifié par l'Admin, on laisse 'e.roles' à null
            // et la méthode createNewEmploye le mettra par défaut à EMPLOYE.
        }

        return createNewEmploye(e);
    }

    /**
     * Crée un nouvel employé pour l'auto-enregistrement (rôle forcé à EMPLOYE).
     */
    public Employe createNewEmployeForPublicRegistration(com.fatou82.suivi.suivihoraireapi.dto.RegisterRequest req) {
        Employe e = new Employe();
        e.setNom(req.getNom());
        e.setPrenom(req.getPrenom());
        e.setEmail(req.getEmail());
        e.setMotDePasse(req.getMotDePasse());
        e.setAdresse(req.getAdresse());

        // ... (votre logique de parsing de date, identique à createNewEmployeFromRegister)

        // Resolve poste by name (identique)
        if (req.getPoste() != null && !req.getPoste().isBlank()) {
            com.fatou82.suivi.suivihoraireapi.entities.Poste p = posteRepository.findByNom(req.getPoste())
                    .orElseThrow(() -> new RuntimeException("Poste introuvable: " + req.getPoste()));
            e.setPoste(p);
        } else {
            throw new RuntimeException("Le poste est requis pour l'inscription.");
        }

        // 🎯 FORCER LE RÔLE EMPLOYE pour la route publique
        Role defaultRole = roleRepository.findByNom(RoleType.EMPLOYE)
                .orElseThrow(() -> new RuntimeException("Rôle EMPLOYE par défaut non trouvé."));
        Set<Role> defaultRoles = new HashSet<>();
        defaultRoles.add(defaultRole);
        e.setRoles(defaultRoles);
        // Note : En forçant ici, la vérification dans createNewEmploye est ignorée

        // Le rôle est déjà défini, on appelle la méthode principale.
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
     * Récupère la liste de tous les employés depuis la base de données.
     * Utilise la méthode findAll() fournie par JpaRepository.
     */
    public List<Employe> findAllEmployes() {
        return employeRepository.findAll();
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
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        adminEmploye.setRoles(adminRoles);

        // 3. Utiliser la logique d'enregistrement existante (hachage et matricule)
        return createNewEmploye(adminEmploye);
    }

    /**
     * Met à jour les informations d'un employé existant.
     * @param id L'identifiant de l'employé à mettre à jour.
     * @param updateReq Le DTO contenant les nouvelles données.
     * @return L'entité Employe mise à jour.
     */
    public Employe updateEmploye(Long id, com.fatou82.suivi.suivihoraireapi.dto.RegisterRequest updateReq) {
        // 1. Trouver l'employé existant
        Employe existingEmploye = employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", "id", id.toString()));

        // 2. Mettre à jour les champs autorisés
        // Note: Le mot de passe n'est mis à jour que s'il est explicitement fourni
        if (updateReq.getMotDePasse() != null && !updateReq.getMotDePasse().isBlank()) {
            existingEmploye.setMotDePasse(passwordEncoder.encode(updateReq.getMotDePasse()));
        }
        
        // Mise à jour des autres champs de base
        existingEmploye.setNom(updateReq.getNom());
        existingEmploye.setPrenom(updateReq.getPrenom());
        existingEmploye.setEmail(updateReq.getEmail()); 
        existingEmploye.setAdresse(updateReq.getAdresse());
        
        // Mise à jour du poste (si fourni)
        if (updateReq.getPoste() != null && !updateReq.getPoste().isBlank()) {
            com.fatou82.suivi.suivihoraireapi.entities.Poste p = posteRepository.findByNom(updateReq.getPoste())
                    .orElseThrow(() -> new RuntimeException("Poste introuvable: " + updateReq.getPoste()));
            existingEmploye.setPoste(p);
        }

        // 3. Enregistrer les modifications
        return employeRepository.save(existingEmploye);
    }

    /**
     * Permet à un utilisateur de modifier son propre profil (auto-modification).
     * Seuls les champs Nom, Prénom, Email et Adresse peuvent être mis à jour.
     * Le changement d'email inclut une vérification d'unicité.
     * * @param email L'email de l'utilisateur connecté (Principal).
     * @param updateReq Les données de mise à jour (DTO).
     * @return L'employé mis à jour.
     */
    public Employe updateSelf(String email, com.fatou82.suivi.suivihoraireapi.dto.RegisterRequest updateReq) {
        
        // 1. Trouver l'employé existant par son email principal
        Employe existingEmploye = employeRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Employe", "email", email));

        // 2. Mise à jour des champs de base
        existingEmploye.setNom(updateReq.getNom());
        existingEmploye.setPrenom(updateReq.getPrenom()); 
        existingEmploye.setAdresse(updateReq.getAdresse());

        // 3. Traitement de l'Email avec Vérification d'Unicité
        if (updateReq.getEmail() != null && !updateReq.getEmail().isBlank()) {
            
            // Vérification d'unicité : on recherche si le nouvel email existe déjà
            Optional<Employe> employeWithNewEmail = employeRepository.findByEmail(updateReq.getEmail());
            
            // Si l'email est trouvé ET que l'ID associé est différent de l'employé actuel, on lance une erreur.
            if (employeWithNewEmail.isPresent() && !employeWithNewEmail.get().getId().equals(existingEmploye.getId())) {
                throw new IllegalArgumentException("L'email fourni est déjà utilisé par un autre employé.");
            }
            
            // Si la vérification passe (ou si l'email n'a pas changé), on met à jour l'email.
            existingEmploye.setEmail(updateReq.getEmail());
        }
        
        // 4. IGNORER les champs sensibles du DTO (Poste, Rôle, DateEmbauche, MotDePasse)
        //    Ces champs ne sont pas affectés ici, car l'employé ne peut pas les modifier lui-même.

        // 5. Sauvegarde
        return employeRepository.save(existingEmploye);
    }

    /**
     * Met à jour l'ensemble des rôles d'un employé.
     * @param id L'identifiant de l'employé.
     * @param roleNames La liste des noms de rôles à assigner.
     * @return L'entité Employe mise à jour.
     */
    public Employe updateEmployeRoles(Long id, List<String> roleNames) {
        Employe existingEmploye = employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", "id", id.toString()));

        // 🚨 1. CAPTURER LES ANCIENS RÔLES AVANT LA MODIFICATION
        String oldRoles = existingEmploye.getRoles().stream()
            .map(r -> r.getNom().name())
            .reduce((a, b) -> a + ", " + b)
            .orElse("AUCUN");

        if (roleNames == null || roleNames.isEmpty()) {
            throw new RuntimeException("Au moins un rôle doit être spécifié.");
        }

        Set<Role> newRoles = new HashSet<>();
        boolean matriculeNeedsUpdate = false;
        
        // 1. Charger et valider tous les rôles
        for (String roleName : roleNames) {
            try {
                RoleType specifiedRole = RoleType.valueOf(roleName.toUpperCase()); 
                Role role = roleRepository.findByNom(specifiedRole)
                        .orElseThrow(() -> new RuntimeException("Rôle spécifié introuvable: " + roleName));
                newRoles.add(role);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Rôle non valide: " + roleName);
            }
        }
        
        // 2. Vérifier si le rôle principal du matricule a changé
        String oldPrefix = getRolePrefix(existingEmploye.getRoles());
        existingEmploye.setRoles(newRoles); // Assigner les nouveaux rôles (nécessaire avant de regénérer le préfixe)
        String newPrefix = getRolePrefix(newRoles);
        
        // 3. Mise à jour du matricule si le rôle principal a changé
        if (!oldPrefix.equals(newPrefix)) {
            String newMatricule = generateMatricule(existingEmploye);
            existingEmploye.setMatricule(newMatricule);
        }
        
        // 4. Enregistrer les modifications
        Employe savedEmploye = employeRepository.save(existingEmploye);
        
        // 🚨 5. JOURNAL D'AUDIT ADMINISTRATIF : Changement de Rôles
        String newRolesListString = savedEmploye.getRoles().stream()
            .map(r -> r.getNom().name())
            .reduce((a, b) -> a + ", " + b)
            .orElse("AUCUN");

        String details = String.format("Rôles de l'employé ID %d (%s %s) mis à jour. Ancien(s) Rôle(s): [%s]. Nouveau(x) Rôle(s): [%s].",
            id, savedEmploye.getPrenom(), savedEmploye.getNom(), oldRoles, newRolesListString); // Utiliser la nouvelle variable

        auditLogService.logAdminAction("UPDATE_ROLES", "Employe", id, details);

        return savedEmploye;
    }
    /**
     * Permet à un utilisateur de modifier son propre mot de passe.
     * @param email L'email de l'utilisateur connecté (Principal).
     * @param req DTO contenant l'ancien et le nouveau mot de passe.
     * @return L'employé mis à jour.
     */
    public Employe changePassword(String email, com.fatou82.suivi.suivihoraireapi.dto.ChangePasswordRequest req) {
        
        Employe existingEmploye = employeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", "email", email));

        // 1. Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(req.getAncienMotDePasse(), existingEmploye.getMotDePasse())) {
            throw new IllegalArgumentException("L'ancien mot de passe est incorrect.");
        }
        
        // 2. Vérifier si le nouveau mot de passe est différent de l'ancien (bonne pratique)
        if (req.getAncienMotDePasse().equals(req.getNouveauMotDePasse())) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent de l'ancien.");
        }

        // 3. Hacher et mettre à jour le nouveau mot de passe
        String hashedPassword = passwordEncoder.encode(req.getNouveauMotDePasse());
        existingEmploye.setMotDePasse(hashedPassword);
        
        // 4. Sauvegarde
        return employeRepository.save(existingEmploye);
    }
    /**
     * Met à jour le mot de passe d'un employé par son ID.
     * Cette méthode ne doit être utilisée que par l'ADMIN ou pour l'auto-update après vérification.
     * @param id L'identifiant de l'employé.
     * @param newPassword Le nouveau mot de passe en clair.
     * @return L'entité Employe mise à jour.
     */
    public Employe updatePassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Le nouveau mot de passe ne peut pas être vide.");
        }
        
        Employe existingEmploye = employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", "id", id.toString()));

        // 1. Hacher le nouveau mot de passe
        String hashedPassword = passwordEncoder.encode(newPassword);
        
        // 2. Mettre à jour et sauvegarder
        existingEmploye.setMotDePasse(hashedPassword);
        Employe savedEmploye = employeRepository.save(existingEmploye); // 👈 Sauvegarder d'abord

        // 🚨 3. JOURNAL D'AUDIT ADMINISTRATIF : Réinitialisation de Mot de Passe
        String details = String.format("Mot de passe de l'employé ID %d (%s %s) réinitialisé par un administrateur.",
            id, savedEmploye.getPrenom(), savedEmploye.getNom());
        auditLogService.logAdminAction("RESET_PASSWORD", "Employe", id, details); // Action: RESET_PASSWORD

        return savedEmploye;
    }
    
    /**
     * Désactive (soft delete) un employé en mettant son statut 'actif' à false.
     * @param id L'identifiant de l'employé à désactiver.
     */
    public void deactivateEmploye(Long id) {
        Employe employeToChange = employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", "id", id.toString()));

        if (employeToChange.getActif()) {
            // Désactivation logique (Soft Delete)
            employeToChange.setActif(false);
            employeRepository.save(employeToChange);
            
            // JOURNAL D'AUDIT ADMINISTRATIF : Désactivation
            String details = String.format("Désactivation de l'employé ID %d (%s %s). Statut: Inactif.", 
                                        id, employeToChange.getPrenom(), employeToChange.getNom());
            auditLogService.logAdminAction("DEACTIVATE_EMPLOYE", "Employe", id, details);
        }
    }

    /**
     * Réactive un employé en mettant son statut 'actif' à true.
     * @param id L'identifiant de l'employé à réactiver.
     * @return L'entité Employe réactivée.
     */
    public Employe reactivateEmploye(Long id) {
        Employe employeToReactivate = employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", "id", id.toString()));

        if (!employeToReactivate.getActif()) {
            // Activation logique
            employeToReactivate.setActif(true);
            Employe savedEmploye = employeRepository.save(employeToReactivate);
            
            // JOURNAL D'AUDIT ADMINISTRATIF : Activation
            String details = String.format("Activation de l'employé ID %d (%s %s). Statut: Actif.", 
                                        id, savedEmploye.getPrenom(), savedEmploye.getNom());
            auditLogService.logAdminAction("ACTIVATE_EMPLOYE", "Employe", id, details);

            return savedEmploye;
        }
    
        return employeToReactivate;
    }
}

