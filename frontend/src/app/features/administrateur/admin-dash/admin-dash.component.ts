import { Component } from '@angular/core';
import { NgIf } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ProfileEditComponent } from '../../../shared/compoments/edit-profil/edit-profil.component';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-admin-dash',
  standalone: true,
  imports: [
    NgIf,
    ReactiveFormsModule,
    ProfileEditComponent,
    RouterOutlet,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './admin-dash.component.html',
  styleUrl: './admin-dash.component.css'
})
export class AdminDashComponent {

  isSidebarCollapsed = false;
  isProfileOpen = false;
  activeTab: 'infos' | 'password' = 'infos';

  toggleSidebar() {
    this.isSidebarCollapsed = !this.isSidebarCollapsed;
  }

  toggleProfileModal(mode: 'infos' | 'password') {
    this.activeTab = mode;
    this.isProfileOpen = !this.isProfileOpen;
  }

  logout() {
    if(confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) {
      localStorage.clear();
      window.location.href = '/login';
    }
  }

}
