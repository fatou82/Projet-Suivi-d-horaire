import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConfigurationService } from '../../../core/services/configuration/configuration.service';

@Component({
  selector: 'app-configuration-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './regle-configuration.component.html',
  styleUrl: './regle-configuration.component.css'
})
export class RegleConfigurationComponent implements OnInit {
  configurations: any[] = [];

  constructor(private configService: ConfigurationService) {}

  ngOnInit() {
    this.loadConfigs();
  }

  loadConfigs() {
    this.configService.getAllConfigs().subscribe(data => {
      this.configurations = data; // Contient nomCle, valeur, description, id
    });
  }

  // Permet de filtrer les configs par thématique dans le HTML
  getConfigsByGroup(identifiers: string[]): any[] {
    if (!this.configurations || this.configurations.length === 0) {
      return [];
    }
    return this.configurations.filter(c =>
      identifiers.some(id => c.nomCle.startsWith(id) || c.nomCle === id)
    );
  }

  // Sauvegarde toutes les configurations
  saveAllConfigs() {
    const confirmMessage = "Voulez-vous appliquer le nouveau solde de congé à TOUS les employés actuels ? \n\n (Annuler appliquera la règle uniquement aux nouveaux)";
    const applyToAll = confirm(confirmMessage);

    // On envoie l'information au service (ajoute le paramètre boolean dans ton service Angular)
    this.configService.updateAllConfigs(this.configurations, applyToAll).subscribe({
      next: () => {
        alert("Paramètres mis à jour avec succès !");
        this.loadConfigs(); // Recharger pour être sûr
      },
      error: (err) => alert("Erreur lors de la mise à jour")
    });
  }
}
