import {Component} from '@angular/core';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {ProfileEditComponent} from '../../../shared/compoments/edit-profil/edit-profil.component';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-employe-dash',
  standalone: true,
  imports: [
    RouterOutlet,
    ProfileEditComponent,
    NgIf,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './employe-dash.component.html',
  styleUrl: './employe-dash.component.css'
})

export class EmployeDashComponent {
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
    if(confirm('Voulez-vous vraiment vous déconnecter de votre espace personnel ?')) {
      localStorage.clear();
      window.location.href = '/login';
    }
  }
}
