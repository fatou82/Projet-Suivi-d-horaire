package com.fatou82.suivi.suivihoraireapi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fatou82.suivi.suivihoraireapi.enums.RoleType;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Audited
@lombok.EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @lombok.EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING) // Stocke le nom de l'Enum ('ADMINISTRATEUR', 'RH', etc.)
    @Column(nullable = false, unique = true)
    private RoleType nom; // nom : string (maintenant un Enum)    
    
    // Relation Many-to-Many inverse avec Employe
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    @JsonIgnore
    private Set<Employe> employes;
}

