import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PosteService } from '../../../core/services/poste/poste.service';

@Component({
  selector: 'app-gerer-postes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gerer-postes.component.html',
  styleUrl: './gerer-postes.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GererPostesComponent implements OnInit {
  postes: any[] = [];
  filteredPostes: any[] = [];
  searchTerm: string = '';
  showForm = false;
  isEditMode = false;
  currentPoste: any = { nom: '', abreviation: '', description: '' };

  constructor(private posteService: PosteService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadPostes();
  }

  loadPostes() {
    this.posteService.getAllPostes().subscribe({
      next: (data) => {
        this.postes = data;
        this.applyFilters();
      },
      error: (err) => console.error('Erreur chargement postes', err)
    });
  }

  applyFilters() {
    const term = this.searchTerm.toLowerCase().trim();
    this.filteredPostes = this.postes.filter(p =>
      p.nom.toLowerCase().includes(term) || p.abreviation.toLowerCase().includes(term)
    );
    this.cdr.markForCheck();
  }

  toggleForm() {
    this.showForm = !this.showForm;
    if (!this.showForm) {
      this.currentPoste = { nom: '', abreviation: '', description: '' };
      this.isEditMode = false;
    }
  }

  editPoste(poste: any) {
    this.isEditMode = true;
    this.currentPoste = { ...poste };
    this.showForm = true;
    this.cdr.detectChanges();
  }

  savePoste() {
    const request = this.isEditMode
      ? this.posteService.updatePoste(this.currentPoste.id, this.currentPoste)
      : this.posteService.createPoste(this.currentPoste);

    request.subscribe({
      next: () => {
        alert(this.isEditMode ? "Poste mis à jour !" : "Poste créé !");
        this.showForm = false;
        this.loadPostes();
      },
      error: (err) => alert(err.error?.message || "Une erreur est survenue")
    });
  }

  deletePoste(id: number) {
    if (confirm("Voulez-vous vraiment supprimer ce poste ?")) {
      this.posteService.deletePoste(id).subscribe({
        next: () => {
          alert("Poste supprimé !");
          this.loadPostes();
        },
        error: (err) => {
          // Ici on capture l'erreur de ton service Backend (employés rattachés)
          alert("Impossible de supprimer : " + (err.error?.message || "Des employés utilisent ce poste."));
        }
      });
    }
  }
}
