package com.fatou82.suivi.suivihoraireapi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Poste {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nom; 
    
    @Column(nullable = false, unique = true, length = 3)
    private String abreviation; // Abréviation pour le matricule (ex: "CPT", "SEC")
    
    private String description;
    
    // Relation inverse : Un Poste peut concerner plusieurs Employés
    @OneToMany(mappedBy = "poste", fetch = FetchType.LAZY)
    private Set<Employe> employes;
}