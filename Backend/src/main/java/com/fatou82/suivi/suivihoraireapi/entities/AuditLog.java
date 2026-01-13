package com.fatou82.suivi.suivihoraireapi.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateAction; 
    
    private String actionType; 
    private String entiteCible; 
    private Long entiteCibleId; 
    @Column(columnDefinition = "TEXT") // Pour stocker un JSON ou un long texte de détails
    private String details;

    // Relation 'generer' avec Employe (acteur de l'action)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "auditLogs", "motDePasse"}) // Ajoute ceci
    private Employe employe; // Un Employe 'genere' 1..* AuditLog
}
