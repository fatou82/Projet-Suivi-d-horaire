package com.fatou82.suivi.suivihoraireapi.entities;

import jakarta.persistence.*; 
import lombok.*;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.envers.Audited;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Audited
@lombok.EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Poste {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @lombok.EqualsAndHashCode.Include
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nom; 
    
    @Column(nullable = false, unique = true, length = 3)
    private String abreviation; // Abréviation pour le matricule (ex: "CPT", "SEC")
    
    private String description;
    
    // Relation inverse : Un Poste peut concerner plusieurs Employés
    @OneToMany(mappedBy = "poste", fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    @JsonIgnore
    private Set<Employe> employes;
}