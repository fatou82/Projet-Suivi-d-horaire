import { Component, OnInit } from '@angular/core';
import { PointageService } from '../../../core/services/pointage/pointage.service';
import { DatePipe, DecimalPipe, NgClass, NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {AuthService} from '../../../core/services/auth.service'; // Indispensable pour le filtre date

@Component({
  selector: 'app-employe-pointage',
  standalone: true,
  templateUrl: './employe-pointage.component.html',
  styleUrls: ['./employe-pointage.component.css'],
  imports: [NgFor, NgClass, DecimalPipe, DatePipe, FormsModule]
})

export class EmployePointageComponent implements OnInit {
  pointagesAffiches: any[] = [];
  idConnecte!: number; // On retire la valeur fixe "24"
  dateFiltre: string = '';
  message: { text: string, type: string } | null = null;

  constructor(
    private pointageService: PointageService,
    private authService: AuthService) {}

  ngOnInit(): void {
    this.recupererUtilisateurLogge();
  }

  recupererUtilisateurLogge(): void {
    const id = this.authService.getEmployeId(); // On utilise notre nouvelle fonction

    if (id) {
      this.idConnecte = id;
      this.chargerTout(); // Maintenant ça ne plantera plus !
    } else {
      this.afficherAlerte("Session invalide, veuillez vous reconnecter", "danger");
    }
  }

  // Charge l'intégralité de l'historique (Bouton "Tout")
  chargerTout(): void {
    this.dateFiltre = '';
    this.pointageService.getMonHistorique(this.idConnecte).subscribe({
      next: (data) => this.pointagesAffiches = data,
      error: (err) => console.error('Erreur de chargement', err)
    });
  }

  // Filtre les résultats localement ou via API si votre back le supporte
  filtrerParDate(): void {
    if (!this.dateFiltre) return;

    this.pointageService.getMonHistorique(this.idConnecte).subscribe({
      next: (data) => {
        // Filtrage local basé sur la date du pointage
        this.pointagesAffiches = data.filter((p: any) =>
          p.heurePointage.startsWith(this.dateFiltre)
        );
      }
    });
  }

  // Vérifie si un type de pointage existe déjà pour AUJOURD'HUI
  estDejaPointe(type: string): boolean {
    const today = new Date().toISOString().split('T')[0];
    return this.pointagesAffiches.some(p =>
      p.typePointage === type && p.heurePointage.startsWith(today)
    );
  }

  fairePointer(type: string): void {
    // Double sécurité : On empêche l'exécution si déjà pointé
    if (this.estDejaPointe(type)) {
      this.afficherAlerte(`Vous avez déjà validé votre ${type} aujourd'hui.`, 'danger');
      return;
    }

    this.pointageService.effectuerPointage(this.idConnecte, type).subscribe({
      next: () => {
        this.afficherAlerte(`Pointage ${type} réussi !`, 'success');
        this.chargerTout();
      },
      error: () => this.afficherAlerte('Erreur lors du pointage', 'danger')
    });
  }

  afficherAlerte(msg: string, type: string): void {
    this.message = { text: msg, type: type };
    setTimeout(() => this.message = null, 3000);
  }
}
