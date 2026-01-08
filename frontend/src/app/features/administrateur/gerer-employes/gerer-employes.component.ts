import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { EmployeService } from '../../../core/services/employe/employe.service';
import { ChangeDetectionStrategy } from '@angular/core';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { PosteService } from '../../../core/services/poste/poste.service';

@Component({
  selector: 'app-gerer-employes',
  standalone: true,
  imports: [FormsModule, CommonModule],
  providers: [EmployeService, PosteService],
  templateUrl: './gerer-employes.component.html',
  styleUrl: './gerer-employes.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GererEmployesComponent implements OnInit {

  private platformId = inject(PLATFORM_ID);

  constructor(
    private employeService: EmployeService,
    private posteService: PosteService,
    private cdr: ChangeDetectorRef
  ) {}

  showForm = false;
  availableRoles = ['ADMINISTRATEUR', 'RESSOURCE_HUMAINE', 'MANAGER', 'EMPLOYE'];
  availablePostes: any[] = [];
  isEditMode = false;
  currentEmployee: any = {}; // L'employé lié au formulaire (ajout ou modif)
  employes: any[] = [];
  isFormRolesOpen = false;
  searchTerm: string = '';
  showInactive = false;

  ngOnInit() {
    // On vérifie qu'on est sur le navigateur
    if (isPlatformBrowser(this.platformId)) {
      const roles = localStorage.getItem('userRoles');
      console.log("Mes rôles actuels :", roles);
    }

    this.loadEmployees();
    this.loadPostes();
  }

  toggleFormRolesDropdown() {
    this.isFormRolesOpen = !this.isFormRolesOpen;
  }

  // Modifie toggleForm pour réinitialiser le dropdown à la fermeture
  toggleForm() {
    this.showForm = !this.showForm;
    this.isFormRolesOpen = false;

    if (!this.showForm) {
      // On vide TOUT quand on ferme pour repartir sur une base saine
      this.currentEmployee = {};
      this.isEditMode = false;
    } else if (!this.isEditMode) {
      // Initialisation par défaut pour un NOUVEL employé uniquement
      this.currentEmployee = {
        roleNames: ['EMPLOYE'],
        posteNom: '',
        actif: true,
        dateEmbauche: new Date().toISOString().split('T')[0]
      };
    }
    this.cdr.detectChanges();
  }

  loadPostes() {
    this.posteService.getAllPostes().subscribe({
      next: (data) => {
        this.availablePostes = data;
        this.cdr.detectChanges(); // Force la mise à jour de la vue
      },
      error: (err) => {
        console.error('Erreur lors de la récupération des postes :', err);
        // Optionnel : remettre des données par défaut en cas d'erreur backend
      }
    });
  }

  loadEmployees() {
    this.employeService.getAllEmployes().subscribe({
      next: (data) => {
        this.employes = data.map(emp => ({
          ...emp,
          isOpened: false,
          rolesDisplay: emp.roleNames?.join(', ') || 'Aucun rôle'
        }));
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Erreur :', err)
    });
  }

  // Gérer la sélection des rôles (Max 2)
  onRoleToggle(role: string, event: any) {
    const isChecked = event.target.checked;

    // On utilise roleNames pour être raccord avec le HTML
    if (!this.currentEmployee.roleNames) {
      this.currentEmployee.roleNames = [];
    }

    if (isChecked) {
      if (this.currentEmployee.roleNames.length >= 2) {
        event.target.checked = false;
        alert("Un employé ne peut pas avoir plus de 2 rôles.");
        return;
      }
      this.currentEmployee.roleNames.push(role);
    } else {
      this.currentEmployee.roleNames = this.currentEmployee.roleNames.filter((r: string) => r !== role);
    }

    // Important : on force la détection de changement pour mettre à jour l'affichage du bouton
    this.cdr.detectChanges();
  }

  // --- CORRECTION : Préparer la modification ---
  // Modifier updateEmployee pour préparer la chaîne de caractères
  updateEmployee(emp: any) {
    this.isEditMode = true;
    this.currentEmployee = {
      ...emp,
      // On copie les noms des rôles pour que la modale les utilise
      roleNames: emp.roleNames ? [...emp.roleNames] : [],
      posteNom: emp.posteNom,
      actif: emp.actif
    };
    this.showForm = true;
    this.cdr.detectChanges();
  }

  // Gère la sauvegarde (création ou modification)
  saveEmployee() {
    if (!this.currentEmployee.posteNom) {
      alert("Veuillez sélectionner un poste.");
      return;
    }

    const isActif = this.currentEmployee.actif !== undefined ? this.currentEmployee.actif : true;

    const payload = {
      nom: this.currentEmployee.nom,
      prenom: this.currentEmployee.prenom,
      email: this.currentEmployee.email,
      adresse: this.currentEmployee.adresse,
      poste: this.currentEmployee.posteNom,
      roleNames: this.currentEmployee.roleNames,
      actif: isActif,
      dateEmbauche: this.currentEmployee.dateEmbauche || new Date().toISOString().split('T')[0]
    };

    // Modification
    if (this.isEditMode) {
      this.employeService.updateEmploye(this.currentEmployee.id, payload).subscribe({
        next: (res) => {
          alert("Employé mis à jour avec succès !");
          this.showForm = false;
          this.isEditMode = false;
          this.currentEmployee = {};

          this.loadEmployees(); // Recharge la liste
          this.cdr.detectChanges(); // Force le rafr
        },
        error: (err) => alert("Erreur modification : " + (err.error?.message || "Erreur serveur"))
      });
    } else {
      // Création
      const createPayload = { ...payload, motDePasse: "Pass123!" };
      this.employeService.createEmploye(createPayload).subscribe({
        next: (res) => {
          alert("Nouvel employé ajouté avec succès !");
          this.toggleForm();
          this.loadEmployees();
        },
        error: (err) => alert("Erreur création : " + (err.error?.message || "Erreur serveur"))
      });
    }
  }

  deleteEmployee(id: number) {
    if(confirm('Êtes-vous sûr de vouloir désactiver cet employé ?')) {
      this.employeService.deleteEmploye(id).subscribe({
        next: () => {
          // CORRECTION : On crée une copie du tableau avec l'élément modifié
          this.employes = this.employes.map(e => {
            if (e.id === id) {
              return { ...e, actif: false }; // On retourne un nouvel objet
            }
            return e;
          });

          alert("Employé désactivé avec succès !");
          this.loadEmployees();
        },
        error: (err) => alert("Erreur lors de la désactivation : " + err.error.message)
      });
    }
  }

  toggleDropdown(emp: any) {
    this.employes.forEach(e => {
      if (e !== emp) e.isOpened = false;
    });
    emp.isOpened = !emp.isOpened;
  }

  toggleRole(emp: any, role: string) {
    const index = emp.roleNames.indexOf(role);
    if (index > -1) {
      if (emp.roleNames.length > 1) emp.roleNames.splice(index, 1);
    } else {
      if (emp.roleNames.length < 2) emp.roleNames.push(role);
    }

    // Optionnel : Sauvegarder immédiatement le changement de rôle
    const updatePayload = { ...emp, poste: emp.posteNom };
    this.employeService.updateEmploye(emp.id, updatePayload).subscribe({
      next: () => {
        this.loadEmployees(); // Pour rafraîchir l'affichage "rolesDisplay"
      }
    });
  }

  // Fonction pour obtenir les employés filtrés
  get filteredEmployes() {
    let list = this.employes;

    // 1. Filtre de statut (Actif vs Tous)
    if (!this.showInactive) {
      list = list.filter(emp => emp.actif === true);
    }

    // 2. Filtre de recherche textuelle
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      list = list.filter(emp =>
        emp.nom?.toLowerCase().includes(term) ||
        emp.prenom?.toLowerCase().includes(term) ||
        emp.matricule?.toLowerCase().includes(term)
      );
    }
    return list;
  }

  // Optionnel : Forcer la détection lors de la saisie
  onSearchChange() {
    this.cdr.detectChanges();
  }

  // Filtrer les inactifs
  toggleInactiveFilter() {
    this.showInactive = !this.showInactive;
    this.cdr.detectChanges();
  }

  // Réactiver un employé
  reactivateEmployee(id: number) {
    if(confirm('Réactiver cet employé ?')) {
      this.employeService.reactivateEmploye(id).subscribe({
        next: () => {
          alert("Employé réactivé !");
          this.loadEmployees(); // On recharge pour voir le changement de statut
        },
        error: (err) => alert("Erreur : " + err.error.message)
      });
    }
  }

}
