import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ConfigurationService {
  private apiUrl = 'http://localhost:8080/api/admin/configurations';

  constructor(private http: HttpClient) {}

  // Récupère toutes les règles (bulk)
  getAllConfigs(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  // Met à jour la liste complète
  updateAllConfigs(configs: any[], applyToAll: boolean): Observable<void> {
    // On ajoute ?applyToAll=true ou false à la fin de l'URL
    return this.http.put<void>(`${this.apiUrl}/bulk?applyToAll=${applyToAll}`, configs);
  }

  // Récupère une règle par sa clé
  getConfigByKey(nomCle: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/key/${nomCle}`);
  }
}
