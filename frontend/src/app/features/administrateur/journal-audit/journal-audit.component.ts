import { Component, OnInit } from '@angular/core';
import {DatePipe, NgClass} from '@angular/common';
import {AuditService} from '../../../core/services/audit/audit.service';
import { CommonModule } from '@angular/common';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-journal-audit',
  standalone: true,
  templateUrl: './journal-audit.component.html',
  imports: [
    NgClass,
    DatePipe,
    CommonModule,
    FormsModule,
  ],
  styleUrls: ['./journal-audit.component.css']
})
export class JournalAuditComponent implements OnInit {
  logs: any[] = [];
  filteredLogs: any[] = [];
  filterType: string = 'TOUT';
  searchTerm: string = '';

  constructor(private auditService: AuditService) {}

  ngOnInit(): void {
    this.auditService.getLogs().subscribe(data => {
      this.logs = data;
      this.filteredLogs = data;
    });
  }

  applyFilter(type?: string) {
    if (type) this.filterType = type;

    this.filteredLogs = this.logs.filter(log => {
      // 1. Filtre par catégorie (Boutons)
      let matchesType = true;

      if (this.filterType === 'ADMIN') {
        // On inclut les Postes ET les configurations (RegleConfiguration)
        // On vérifie aussi l'actionType pour être sûr de ne rien rater
        matchesType =
          log.entiteCible === 'Poste' ||
          log.entiteCible === 'RegleConfiguration' ||
          log.actionType.includes('CREATE') ||
          log.actionType.includes('UPDATE_CONFIGS');
      } else if (this.filterType === 'MANUEL') {
        matchesType = log.actionType.includes('ACTIVATE') || log.actionType.includes('DEACTIVATE');
      }

      // 2. Filtre par recherche textuelle
      const term = this.searchTerm.toLowerCase();
      const matchesSearch =
        (log.acteur?.toLowerCase().includes(term)) ||
        (log.actionType?.toLowerCase().includes(term)) ||
        (log.details?.toLowerCase().includes(term)) ||
        (log.entiteCible?.toLowerCase().includes(term));

      return matchesType && matchesSearch;
    });
  }
}
