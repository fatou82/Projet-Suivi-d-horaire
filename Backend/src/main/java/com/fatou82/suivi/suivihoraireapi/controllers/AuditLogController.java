package com.fatou82.suivi.suivihoraireapi.controllers;

import com.fatou82.suivi.suivihoraireapi.entities.AuditLog;
import com.fatou82.suivi.suivihoraireapi.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        // On récupère tout, trié par date décroissante (le plus récent en haut)
        List<AuditLog> logs = auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "dateAction"));
        return ResponseEntity.ok(logs);
    }
}