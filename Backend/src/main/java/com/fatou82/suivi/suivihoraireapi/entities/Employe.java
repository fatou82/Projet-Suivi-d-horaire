package com.fatou82.suivi.suivihoraireapi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@lombok.EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Employe {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @lombok.EqualsAndHashCode.Include
    private Long id;

    private String nom;
    private String prenom;

    @Column(nullable =  false, unique = true)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    @Column(nullable = false, unique = true)
    private String matricule;  //matricule : string (Format : EP-CPT-0001)

    private LocalDate dateEmbauche;
    
    private Integer soldeConge;
    private String adresse;
    private Boolean actif = true;

    // Relation MANY-TO-MANY (Employe <-> Role)
    @ManyToMany(fetch = FetchType.EAGER) // Charger les rôles immédiatement pour la sécurité
    @JoinTable(
        name = "employe_role", // Nom de la table de jointure
        joinColumns = @JoinColumn(name = "employe_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @lombok.ToString.Exclude
    private Set<Role> roles; // Un Employe peut avoir plusieurs Roles

    // Relation avec Poste (Many-to-One)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "poste_id", nullable = false)
    @lombok.ToString.Exclude
    private Poste poste; // Un Employe est rattaché à 1 Poste

    // Relation avec DemandeConge (1 Employe peut 'faire' plusieurs DemandeConge)
    @OneToMany(mappedBy = "demandeur", fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    private Set<DemandeConge> demandesConge;

    // Relation avec Absence (1 Employe 'possede' plusieurs Absence)
    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    private Set<Absence> absences;

    // Relation avec Pointage (1 Employe 'effectue' plusieurs Pointage)
    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    private Set<Pointage> pointages;

    // Relation avec AuditLog (1 Employe 'genere' plusieurs AuditLog)
    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    private Set<AuditLog> auditLogs;

    // Relation avec CongeEffectif (1 Employe 'avoir' plusieurs CongeEffectif)
    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    private Set<CongeEffectif> congeEffectifs;

}
