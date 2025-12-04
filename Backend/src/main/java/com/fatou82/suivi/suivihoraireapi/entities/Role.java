package com.fatou82.suivi.suivihoraireapi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

import com.fatou82.suivi.suivihoraireapi.enums.RoleType;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) // Stocke le nom de l'Enum ('ADMINISTRATEUR', 'RH', etc.)
    @Column(nullable = false, unique = true)
    private RoleType nom; // nom : string (maintenant un Enum)    
    
    // Relation Many-to-Many inverse avec Employe
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<Employe> employes;
}

