package com.fatou82.suivi.suivihoraireapi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    private LocalDateTime dateAction; 
    
    private String actionType; 
    private String entiteCible; 
    private String description; 

    // Relation 'generer' avec Employe (acteur de l'action)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe; // Un Employe 'genere' 1..* AuditLog
}
