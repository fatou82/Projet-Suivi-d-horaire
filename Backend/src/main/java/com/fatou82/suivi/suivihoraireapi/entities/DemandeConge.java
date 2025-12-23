package com.fatou82.suivi.suivihoraireapi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;
import org.hibernate.envers.Audited;

import com.fatou82.suivi.suivihoraireapi.enums.DemandeStatus;

@Entity
@Audited
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeConge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DemandeStatus statut; // Enum (EN_ATTENTE, VALIDEE, REFUSEE)

    private LocalDate dateDemande;

    // Relation avec CongeEffectif (1 Demande peut générer plusieurs jours effectifs)
    @OneToMany(mappedBy = "demandeConge", fetch = FetchType.LAZY) 
    // MappedBy: L'entité CongeEffectif possède la clé étrangère
    private Set<CongeEffectif> congesEffectifs; // Relation 1-à-* (One-to-Many)
   
    // Relation 'fait' avec Employe (demandeur)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe demandeur;

    // Relation 'estDeType' avec TypeConge
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_conge_id", nullable = false)
    private TypeConge typeConge;

}
