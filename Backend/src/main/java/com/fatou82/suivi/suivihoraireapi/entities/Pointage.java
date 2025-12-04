package com.fatou82.suivi.suivihoraireapi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.fatou82.suivi.suivihoraireapi.enums.PointageType;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pointage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    private LocalDateTime heurePointage; 
    
    @Enumerated(EnumType.STRING)
    private PointageType typePointage; // Enum (ENTREE, DEBUT_PAUSE, FIN_PAUSE, SORTIE)
    
    private Float dureeTotale; 
    private String source; 

    // Relation 'effectue' avec Employe
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe; // Un Employe 'effectue' 1..* Pointage
}
