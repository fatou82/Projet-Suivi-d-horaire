import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class EmployeService {
  private apiUrl = 'http://localhost:8080/api/employes';
  private authUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  // --- PARTIE ADMIN (Back-office) ---

  // Pour POST /api/employes (Création par Admin/RH)
  createEmploye(data: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, data);
  }

  // Pour GET /api/employes (Liste complète)
  getAllEmployes(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  // Pour PUT /api/employes/{id} (Mise à jour par Admin/RH)
  updateEmploye(id: number, data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, data);
  }

  // Pour DELETE /api/employes/{id}
  deleteEmploye(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  // Pour PATCH réactivation
  reactivateEmploye(id: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/reactivate`, {});
  }

  // --- PARTIE SELF-SERVICE (Profil) ---

  // Pour PUT /api/employes/me (Mise à jour par l'employé lui-même)
  updateSelf(data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/me`, data);
  }

  // Pour PATCH /api/auth/change-password (Changement de mot de passe)
  changePassword(data: any): Observable<any> {
    return this.http.patch(`${this.authUrl}/change-password`, data);
  }

  // Pour GET /api/auth/me (Récupérer les infos de l'employé connecté)
  getMe(): Observable<any> {
    return this.http.get(`${this.authUrl}/me`);
  }
}
