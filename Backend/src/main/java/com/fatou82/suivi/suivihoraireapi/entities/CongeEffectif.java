package com.fatou82.suivi.suivihoraireapi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CongeEffectif {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateConge;

    private Boolean estDeductible;

    // Relation 'generer' avec DemandeConge
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_conge_id", nullable = false)
    private DemandeConge demandeConge;

    // Relation 'avoir' avec Employe
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe; // Un Employe 'avoir' 1..* CongeEffectif

    // Relation 'estDeType' avec TypeConge (Relation indirecte via DemandeConge ou ajoutée pour la clarté)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_conge_id", nullable = false)
    private TypeConge typeConge;

}
