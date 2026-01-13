package com.fatou82.suivi.suivihoraireapi.controllers;

import com.fatou82.suivi.suivihoraireapi.dto.PointageRequest;
import com.fatou82.suivi.suivihoraireapi.entities.Employe;
import com.fatou82.suivi.suivihoraireapi.entities.Pointage;
import com.fatou82.suivi.suivihoraireapi.enums.PointageType;
import com.fatou82.suivi.suivihoraireapi.repositories.EmployeRepository;
import com.fatou82.suivi.suivihoraireapi.services.PointageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/pointages")
@RequiredArgsConstructor
public class PointageController {

    private final PointageService pointageService;
    private final EmployeRepository employeRepository;

    @PostMapping("/pointer")
    public ResponseEntity<Pointage> pointer(@RequestBody PointageRequest request) {
        // 1. Récupérer l'employé complet en base
        Employe employe = employeRepository.findById(request.getEmployeId())
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        // 2. Passer l'objet complet au service
        Pointage p = pointageService.effectuerPointage(employe, request.getType());

        return ResponseEntity.ok(p);
    }

    @GetMapping("/mon-historique")
    public ResponseEntity<List<Pointage>> getMonHistorique(
            @RequestParam Long employeId,
            @RequestParam(required = false) String date) {

        LocalDateTime start;
        LocalDateTime end;

        if (date != null) {
            // Si une date est fournie (ex: "2026-01-13"), on filtre de 00h00 à 23h59
            LocalDate d = LocalDate.parse(date);
            start = d.atStartOfDay();
            end = d.atTime(LocalTime.MAX);
        } else {
            // Par défaut : Uniquement les pointages d'aujourd'hui
            start = LocalDate.now().atStartOfDay();
            end = LocalDateTime.now();
        }

        return ResponseEntity.ok(pointageService.getPointagesEmploye(employeId, start, end));
    }
}
