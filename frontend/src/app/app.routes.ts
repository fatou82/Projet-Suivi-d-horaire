import { Routes } from '@angular/router';
import {AdminDashComponent} from './features/administrateur/admin-dash/admin-dash.component';
import {LoginComponent} from './core/auth/login/login.component';
import {RhDashComponent} from './features/ressource-humaine/rh-dash/rh-dash.component';
import {ManagerDashComponent} from './features/manager/manager-dash/manager-dash.component';
import {EmployeDashComponent} from './features/employe/employe-dash/employe-dash.component';
import {GererEmployesComponent} from './features/administrateur/gerer-employes/gerer-employes.component';
import {StatsComponent} from './features/administrateur/stats/stats.component';
import {GererPostesComponent} from './features/administrateur/gerer-postes/gerer-postes.component';
import {JournalAuditComponent} from './features/administrateur/journal-audit/journal-audit.component';
import { RegleConfigurationComponent} from './features/administrateur/regle-configuration/regle-configuration.component';
import {EmployeStatsComponent} from './features/employe/employe-stats/employe-stats.component';
import {EmployePointageComponent} from './features/employe/employe-pointage/employe-pointage.component';
import {ProfileEditComponent} from './shared/compoments/edit-profil/edit-profil.component';

export const routes: Routes = [
  //Si l'URL est vide '
  { path: '', redirectTo : 'login', pathMatch: 'full' },

  // Ondefinit la route pour le composant Login
  { path: 'login', component: LoginComponent },

  // Ondefinit la route pour le composant AdminDash
  {
    path: 'admin-dash',
    component: AdminDashComponent,
    children: [
      { path: 'dashboard', component: StatsComponent }, // Par défaut on voit les stats
      { path: 'gerer-employes', component: GererEmployesComponent },
      { path: 'gerer-postes', component: GererPostesComponent },
      { path: 'journal-audit', component: JournalAuditComponent },
      { path: 'regle-configuration', component: RegleConfigurationComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  // Routes pour les autres dashboards
  { path: 'rh-dash', component: RhDashComponent },

  // Routes pour les autres dashboards
  { path: 'manager-dash', component: ManagerDashComponent },

  // Routes pour les autres dashboards
  {
    path: 'employe-dash',
    component: EmployeDashComponent,
    children: [
      { path: 'dashboard', component: EmployeStatsComponent },
      { path: 'pointage', component: EmployePointageComponent }, // Le composant avec les 4 boutons
      { path: 'profil', component: ProfileEditComponent }, // Composant partagé
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  }
];
