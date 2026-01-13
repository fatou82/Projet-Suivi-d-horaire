import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-employe-stats',
  imports: [],
  templateUrl: './employe-stats.component.html',
  styleUrl: './employe-stats.component.css'
})
export class EmployeStatsComponent implements OnInit {
  stats = {
    soldeConges: 22,
    heuresSemaine: 0,
    statut: 'Non Pointé',
    alerteRetard: false
  };

  ngOnInit() {
    this.calculerStats();
  }

  calculerStats() {
    // Ici, tu appelleras ton service pour récupérer le cumul des 'dureeTotale'
    // de la semaine courante.
    this.stats.heuresSemaine = 35.5; // Exemple statique
  }
}
