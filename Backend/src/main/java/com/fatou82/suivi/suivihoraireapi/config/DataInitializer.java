package com.fatou82.suivi.suivihoraireapi.config;

import com.fatou82.suivi.suivihoraireapi.entities.Employe;
import com.fatou82.suivi.suivihoraireapi.entities.Poste;
import com.fatou82.suivi.suivihoraireapi.entities.Role;
import com.fatou82.suivi.suivihoraireapi.enums.RoleType;
import com.fatou82.suivi.suivihoraireapi.repositories.EmployeRepository;
import com.fatou82.suivi.suivihoraireapi.repositories.PosteRepository;
import com.fatou82.suivi.suivihoraireapi.repositories.RoleRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

/**
 * Initialise les données essentielles à l'application : rôles, poste par défaut,
 * et crée un administrateur initial si aucun administrateur n'existe.
 *
 * Ceci remplace l'utilisation d'un endpoint "register-admin" pour l'initialisation
 * unique en dev/prod. Les valeurs par défaut peuvent être modifiées ici.
 */
@Component
@Profile("init") // actif uniquement quand le profile 'init' est explicitement activé
@RequiredArgsConstructor
@Order(1)
@Transactional
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final PosteRepository posteRepository;
    private final EmployeRepository employeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1) Seed roles
        for (RoleType rt : RoleType.values()) {
            roleRepository.findByNom(rt).orElseGet(() -> {
                Role r = new Role();
                r.setNom(rt);
                Role saved = roleRepository.save(r);
                log.info("Seeded role: {}", saved.getNom());
                return saved;
            });
        }

        // 2) Ensure a default poste exists for admin (used to generate matricule)
        String adminPosteName = "Administrateur";
        Poste adminPoste = posteRepository.findByNom(adminPosteName).orElseGet(() -> {
            Poste p = new Poste();
            p.setNom(adminPosteName);
            p.setAbreviation("ADM");
            Poste saved = posteRepository.save(p);
            log.info("Created default poste for admin: {} ({})", saved.getNom(), saved.getAbreviation());
            return saved;
        });

        // 3) Create initial admin if none exists
        boolean adminExists = employeRepository.existsByRolesNom(RoleType.ADMINISTRATEUR);
        if (!adminExists) {
            log.info("No administrator found — creating initial admin.");

            Employe admin = new Employe();
            admin.setNom("Fole");
            admin.setPrenom("Fatoumata");
            admin.setEmail("fatoumatafole@gmail.com");
            admin.setMotDePasse(passwordEncoder.encode("admin123")); // hashed here
            admin.setPoste(adminPoste);
            admin.setDateEmbauche(LocalDate.now());

            // assign admin role if present
            Role adminRole = roleRepository.findByNom(RoleType.ADMINISTRATEUR).orElse(null);
            if (adminRole != null) {
                admin.setRoles(Collections.singleton(adminRole));
            }

            // generate matricule similar to EmployeService logic (safe single-use seed)
            int seq = 1;
            Optional<Employe> last = employeRepository.findTopByPoste_IdOrderByIdDesc(adminPoste.getId());
            if (last.isPresent() && last.get().getMatricule() != null) {
                try {
                    String numPart = last.get().getMatricule();
                    numPart = numPart.substring(numPart.lastIndexOf('-') + 1);
                    seq = Integer.parseInt(numPart) + 1;
                } catch (Exception ignored) {
                }
            }
            String numPadded = String.format("%04d", seq);
            String matricule = "AD-" + adminPoste.getAbreviation().toUpperCase() + "-" + numPadded;
            admin.setMatricule(matricule);

            try {
                employeRepository.save(admin);
                log.info("Initial administrator created with email: {} and matricule {} (please change the password immediately)", admin.getEmail(), admin.getMatricule());
            } catch (Exception e) {
                log.error("Failed to create initial admin: {}", e.getMessage());
            }
        } else {
            log.info("Administrator already exists — skipping initial admin creation.");
        }
    }
}
