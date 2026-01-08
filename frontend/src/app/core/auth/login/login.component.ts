import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import {FormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';

@Component({   selector: 'app-login',
  imports: [
    FormsModule,
    CommonModule
  ],
  standalone: true,
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  loginData = { email: '', motDePasse: '' };

  constructor(private authService: AuthService, private router: Router) {}

  onLogin() {
    this.authService.login(this.loginData).subscribe({
      next: (response) => {
        this.authService.saveToken(response.token);
        this.authService.saveUserRoles(response.roles); // On stocke les rôles pour plus tard

        this.redirectUserBasedOnRole(response.roles);
      },
      error: (err) => alert('Erreur : ' + err.error.message)
    });
  }

  private redirectUserBasedOnRole(roles: string[]) {
    // 1. Priorité la plus haute : Administrateur
    if (roles.includes('ROLE_ADMINISTRATEUR')) {
      this.router.navigate(['/admin-dash']);
    }
    // 2. Deuxième priorité : RH
    else if (roles.includes('ROLE_RESSOURCE_HUMAINE')) {
      this.router.navigate(['/rh-dash']);
    }
    // 3. Cas particulier : Manager (même s'il est aussi employé)
    else if (roles.includes('ROLE_MANAGER')) {
      this.router.navigate(['/manager-dash']);
    }
    // 4. Par défaut : Employé
    else {
      this.router.navigate(['/employe-dash']);
    }
  }
}
