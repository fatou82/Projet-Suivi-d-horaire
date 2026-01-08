import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core'; // Vérifie bien ces imports
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { EmployeService } from '../../../core/services/employe/employe.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-edit-profil',
  standalone: true,
  templateUrl: './edit-profil.component.html',
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  styleUrls: ['./edit-profil.component.css']
})
export class ProfileEditComponent implements OnInit {
  // C'est ICI qu'on définit les propriétés pour le parent
  @Input() isVisible: boolean = false;
  @Input() initialTab: 'infos' | 'password' = 'infos';
  @Output() closeRequested = new EventEmitter<void>();

  profileForm!: FormGroup;

  constructor(private fb: FormBuilder, private employeService: EmployeService) {}

  ngOnInit(): void {
    this.initForm();
  }

  // Détecte quand l'input isVisible change
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isVisible'] && changes['isVisible'].currentValue === true) {
      this.loadUserData();
    }
  }

  initForm() {
    this.profileForm = this.fb.group({
      // Champs modifiables
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      adresse: [''],

      // Nouveaux champs en lecture seule (on ne met pas de validateurs car ils ne changeront pas)
      matricule: [{value: '', disabled: true}],
      dateEmbauche: [{value: '', disabled: true}],
      soldeConge: [{value: '', disabled: true}],
      posteNom: [{value: '', disabled: true}],
      roles: [{value: '', disabled: true}],

      // Sécurité
      oldPassword: [''],
      newPassword: ['', [Validators.minLength(6)]],
      confirmPassword: ['']
    });
  }

  loadUserData() {
    this.employeService.getMe().subscribe({
      next: (user) => {
        this.profileForm.patchValue({
          nom: user.nom,
          prenom: user.prenom,
          email: user.email,
          adresse: user.adresse,
          matricule: user.matricule,
          dateEmbauche: user.dateEmbauche,
          soldeConge: user.soldeConge,
          posteNom: user.posteNom,
          // On joint les rôles par une virgule pour l'affichage
          roles: user.roleNames ? user.roleNames.join(', ') : 'Aucun rôle'
        });
      },
      error: (err) => console.error('Erreur de récupération profil', err)
    });
  }

  // Cette fonction est appelée par la croix (✖) dans ton HTML partagé
  close() {
    this.closeRequested.emit();
  }

  onSubmit() {
    if (this.profileForm.valid) {
      this.employeService.updateSelf(this.profileForm.value).subscribe({
        next: (res) => alert('Profil mis à jour !'),
        error: (err) => alert('Erreur lors de la mise à jour')
      });
    }
  }

  onSubmitProfile() {
    const profileData = {
      nom: this.profileForm.value.nom,
      prenom: this.profileForm.value.prenom,
      email: this.profileForm.value.email,
      adresse: this.profileForm.value.adresse
    };

    this.employeService.updateSelf(profileData).subscribe({
      next: () => {
        alert('Informations mises à jour !');
        this.close();
      },
      error: (err) => alert(err.error.message || 'Erreur lors de la mise à jour')
    });
  }

  onSubmitPassword() {
    const { oldPassword, newPassword, confirmPassword } = this.profileForm.value;

    // 1. Vérification locale de correspondance
    if (newPassword !== confirmPassword) {
      alert('Le nouveau mot de passe et la confirmation ne correspondent pas !');
      return;
    }

    // 2. Vérification si les champs ne sont pas vides
    if (!oldPassword || !newPassword) {
      alert('Veuillez remplir tous les champs de mot de passe.');
      return;
    }

    const pwdData = {
      ancienMotDePasse: oldPassword,
      nouveauMotDePasse: newPassword,
      confirmationMotDePasse: confirmPassword
    };

    this.employeService.changePassword(pwdData).subscribe({
      next: () => {
        alert('Mot de passe changé avec succès !');
        // On vide uniquement les champs password
        this.profileForm.patchValue({ oldPassword: '', newPassword: '', confirmPassword: '' });
        this.close();
      },
      error: (err) => alert(err.error.message || 'Erreur lors du changement de mot de passe')
    });
  }
}
