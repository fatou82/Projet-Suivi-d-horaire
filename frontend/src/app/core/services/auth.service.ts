import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({providedIn: 'root'})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {
  }

  login(credentials: { email: string; motDePasse: string }): Observable<any> {
    // On appelle l'endpoint que tu as défini dans ton AuthController
    return this.http.post(`${this.apiUrl}/login`, credentials);
  }

  saveToken(token: string) {
    localStorage.setItem('token', token);
  }

  saveUserRoles(roles: string[]) {
    localStorage.setItem('userRoles', JSON.stringify(roles));
  }

  getUserRoles(): string[] {
    const roles = localStorage.getItem('userRoles');
    return roles ? JSON.parse(roles) : [];
  }

  // Méthode pour extraire l'employeId du token JWT
  getEmployeId(): number | null {
    const token = localStorage.getItem('token');
    if (!token) return null;

    try {
      // Un JWT est composé de 3 parties séparées par des points. La 2ème (index 1) contient les données.
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function (c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));

      const decoded = JSON.parse(jsonPayload);
      return decoded.employeId; // On récupère le champ qu'on a ajouté en Java
    } catch (e) {
      console.error("Erreur lors du décodage du token", e);
      return null;
    }
  }
}
