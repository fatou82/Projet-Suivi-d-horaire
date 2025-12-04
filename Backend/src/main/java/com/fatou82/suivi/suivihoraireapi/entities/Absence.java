package com.fatou82.suivi.suivihoraireapi.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

import com.fatou82.suivi.suivihoraireapi.enums.AbsenceStatus;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Absence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id : long

    private LocalDate dateAbsence; 
    
    private Boolean estJustifiee;

    private LocalDate dateJustification;
    
    private String motif; 
    
    @Enumerated(EnumType.STRING)
    private AbsenceStatus statut; // Enum (ABSENCE_JUSTIFIEE, RETARD, etc.)

    // Relation 'possede' avec Employe
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe; // Un Employe 'possede' 1..* Absence
}
