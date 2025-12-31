import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Fattura } from '../models/fattura.model';

@Injectable({
  providedIn: 'root'
})
export class FatturaService {
  private apiUrl = 'http://localhost:8080/api/fatture';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Fattura[]> {
    return this.http.get<Fattura[]>(this.apiUrl);
  }

  getById(id: number): Observable<Fattura> {
    return this.http.get<Fattura>(`${this.apiUrl}/${id}`);
  }

  search(keyword: string): Observable<Fattura[]> {
    return this.http.get<Fattura[]>(`${this.apiUrl}/search?keyword=${keyword}`);
  }

  getByAnno(anno: number): Observable<Fattura[]> {
    return this.http.get<Fattura[]>(`${this.apiUrl}/anno/${anno}`);
  }

  create(fattura: Fattura): Observable<Fattura> {
    return this.http.post<Fattura>(this.apiUrl, fattura);
  }

  update(id: number, fattura: Fattura): Observable<Fattura> {
    return this.http.put<Fattura>(`${this.apiUrl}/${id}`, fattura);
  }

  updateStato(id: number, stato: string): Observable<Fattura> {
    return this.http.patch<Fattura>(`${this.apiUrl}/${id}/stato`, { stato });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  generaPdf(id: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/genera-pdf`, {});
  }

  downloadPdf(id: number): void {
    window.open(`${this.apiUrl}/${id}/download-pdf`, '_blank');
  }

  getStatistiche(): Observable<any> {
    return this.http.get(`${this.apiUrl}/stats`);
  }
}
