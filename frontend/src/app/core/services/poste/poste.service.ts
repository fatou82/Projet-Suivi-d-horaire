import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PosteService {
  private apiUrl = 'http://localhost:8080/api/postes'; // Ajuste selon ton URL backend

  constructor(private http: HttpClient) {}

  // Pour GET /api/postes (Liste complète)
  getAllPostes(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  // Pour POST /api/postes (Création d'un poste)
  createPoste(data: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, data);
  }

  // Pour PUT /api/postes/{id} (Mise à jour d'un poste)
  updatePoste(id: number, data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, data);
  }

  // Pour DELETE /api/postes/{id} (Suppression d'un poste)
  deletePoste(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
