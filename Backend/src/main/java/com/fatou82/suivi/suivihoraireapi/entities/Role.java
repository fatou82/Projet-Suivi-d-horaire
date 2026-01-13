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
    private Long id; // On retire l'include ici

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    @lombok.EqualsAndHashCode.Include // ON MET L'INCLUDE ICI
    private RoleType nom;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @lombok.ToString.Exclude
    @JsonIgnore
    private Set<Employe> employes;
}