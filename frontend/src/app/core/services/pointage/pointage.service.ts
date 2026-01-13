import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PointageService {

  private apiUrl = 'http://localhost:8080/api/pointages';
  constructor(private http: HttpClient) {}

  // Méthode pour pointer POST /pointer
  effectuerPointage(employeId: number, type: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/pointer`, { employeId, type });
  }

  // Méthode pour récupére son historique GET /mon-historique
  getMonHistorique(employeId: number, date?: string): Observable<any[]> {
    // Vérification de sécurité
    if (employeId === undefined || employeId === null) {
      console.error("getMonHistorique appelé avec un ID invalide !");
      return new Observable<any[]>(sub => sub.next([]));
    }

    let params = new HttpParams().set('employeId', employeId.toString());
    if (date) params = params.set('date', date);

    return this.http.get<any[]>(`${this.apiUrl}/mon-historique`, { params });
  }
}
