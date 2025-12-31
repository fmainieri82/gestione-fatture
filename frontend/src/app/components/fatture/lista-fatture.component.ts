import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Fattura } from '../../models/fattura.model';
import { FatturaService } from '../../services/fattura.service';

@Component({
  selector: 'app-lista-fatture',
  standalone: true,
  imports: [
    CommonModule, 
    RouterLink, 
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatCardModule,
    MatTableModule,
    MatTooltipModule
  ],
  template: `
    <div class="card">
      <div class="card-header flex justify-between align-center">
        <h2>Gestione Fatture e Preventivi</h2>
        <a routerLink="/fatture/nuova">
          <button mat-raised-button color="primary">
            <mat-icon>add</mat-icon>
            Nuova Fattura
          </button>
        </a>
      </div>

      <!-- Filtri -->
      <div class="filters">
        <mat-form-field appearance="outline" class="search-field">
          <mat-label>Cerca fattura...</mat-label>
          <input matInput 
            [(ngModel)]="searchText" 
            (input)="cerca()">
          <mat-icon matPrefix>search</mat-icon>
        </mat-form-field>
        
        <mat-form-field appearance="outline" class="filter-field">
          <mat-label>Anno</mat-label>
          <mat-select [(ngModel)]="annoFiltro" (selectionChange)="filtraPerAnno()">
            <mat-option value="">Tutti gli anni</mat-option>
            <mat-option *ngFor="let anno of anni" [value]="anno">{{ anno }}</mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <!-- Statistiche -->
      <div *ngIf="stats" class="stats-box">
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-label">Fatture Anno Corrente</div>
            <div class="stat-value">{{ stats.fattureAnnoCorrente }}</div>
          </mat-card-content>
        </mat-card>
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-label">Fatturato Anno</div>
            <div class="stat-value">€ {{ stats.fatturatoAnnoCorrente | number:'1.2-2' }}</div>
          </mat-card-content>
        </mat-card>
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-label">Totale Fatture</div>
            <div class="stat-value">{{ stats.totale }}</div>
          </mat-card-content>
        </mat-card>
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-label">Fatturato Totale</div>
            <div class="stat-value">€ {{ stats.fatturatoTotale | number:'1.2-2' }}</div>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Tabella Fatture -->
      <table class="data-table">
        <thead>
          <tr>
            <th>Numero</th>
            <th>Tipo</th>
            <th>Data</th>
            <th>Cliente</th>
            <th>Totale</th>
            <th>Stato</th>
            <th>Azioni</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let fattura of fatture">
            <td><strong>{{ fattura.numeroDocumento }}</strong></td>
            <td>{{ fattura.tipoDocumento }}</td>
            <td>{{ fattura.dataDocumento | date:'dd/MM/yyyy' }}</td>
            <td>{{ fattura.cliente.ragioneSociale }}</td>
            <td><strong>€ {{ fattura.totaleDocumento | number:'1.2-2' }}</strong></td>
            <td>
              <mat-chip [class]="'chip-' + getStatoClass(fattura.stato)">
                {{ fattura.stato }}
              </mat-chip>
            </td>
            <td>
              <div class="action-buttons">
                <button mat-raised-button color="primary" (click)="generaPdf(fattura.id!)" 
                        matTooltip="Genera PDF">
                  <mat-icon>description</mat-icon>
                  PDF
                </button>
                <button mat-raised-button color="primary" (click)="generaPdfSopralluogo(fattura.id!)" 
                        matTooltip="Genera PDF con Scheda di Sopralluogo">
                  <mat-icon>assignment</mat-icon>
                  PDF + Sopralluogo
                </button>
                <button *ngIf="fattura.filePdfPath" 
                        mat-icon-button color="primary" 
                        (click)="downloadPdf(fattura.id!)"
                        matTooltip="Scarica PDF">
                  <mat-icon>download</mat-icon>
                </button>
                <a [routerLink]="['/fatture/edit', fattura.id]">
                  <button mat-icon-button color="primary" matTooltip="Modifica">
                    <mat-icon>edit</mat-icon>
                  </button>
                </a>
                <button mat-icon-button color="warn" (click)="elimina(fattura.id!)" matTooltip="Elimina">
                  <mat-icon>delete</mat-icon>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div *ngIf="fatture.length === 0" class="text-center mt-20">
        Nessuna fattura trovata. <a routerLink="/fatture/nuova">Crea la prima fattura</a>
      </div>
    </div>
  `,
  styles: [`
    .filters {
      display: flex;
      gap: 16px;
      margin: 20px 0;
    }
    
    .search-field {
      flex: 1;
    }
    
    .search-field ::ng-deep .mat-mdc-form-field {
      width: 100%;
    }
    
    .filter-field {
      min-width: 180px;
    }
    
    .filter-field ::ng-deep .mat-mdc-form-field {
      width: 100%;
    }
    
    .stats-box {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 16px;
      margin: 20px 0;
    }
    
    .stat-card {
      text-align: center;
      padding: 0 !important;
    }
    
    .stat-card ::ng-deep .mat-mdc-card-content {
      padding: 20px !important;
    }
    
    .stat-label {
      font-size: 12px;
      color: #666;
      margin-bottom: 8px;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #3f51b5;
      margin: 0;
    }
    
    .chip-bozza {
      background-color: #ffc107 !important;
      color: #000 !important;
    }
    
    .chip-emessa {
      background-color: #4caf50 !important;
      color: #fff !important;
    }
    
    .chip-pagata {
      background-color: #2196f3 !important;
      color: #fff !important;
    }
    
    .chip-annullata {
      background-color: #f44336 !important;
      color: #fff !important;
    }
    
    .action-buttons {
      display: flex;
      gap: 4px;
      align-items: center;
    }
    
    .action-buttons button {
      min-width: auto;
    }
    
    .data-table {
      margin-top: 20px;
    }
    
    .data-table th {
      background-color: #f5f5f5;
      font-weight: 500;
      color: #333;
    }
    
    .data-table td {
      vertical-align: middle;
    }
  `]
})
export class ListaFattureComponent implements OnInit {
  fatture: Fattura[] = [];
  searchText = '';
  annoFiltro = '';
  anni: number[] = [];
  stats: any = null;

  constructor(private fatturaService: FatturaService) {}

  ngOnInit() {
    this.caricaFatture();
    this.caricaStatistiche();
    this.generaAnni();
  }

  caricaFatture() {
    this.fatturaService.getAll().subscribe(
      data => this.fatture = data,
      error => console.error('Errore caricamento fatture:', error)
    );
  }

  cerca() {
    if (this.searchText.length > 0) {
      this.fatturaService.search(this.searchText).subscribe(
        data => this.fatture = data
      );
    } else {
      this.caricaFatture();
    }
  }

  filtraPerAnno() {
    if (this.annoFiltro) {
      this.fatturaService.getByAnno(parseInt(this.annoFiltro)).subscribe(
        data => this.fatture = data
      );
    } else {
      this.caricaFatture();
    }
  }

  generaPdf(id: number) {
    this.fatturaService.generaPdf(id).subscribe(
      response => {
        alert('PDF generato con successo!');
        this.caricaFatture();
      },
      error => alert('Errore generazione PDF: ' + error.error)
    );
  }

  generaPdfSopralluogo(id: number) {
    this.fatturaService.generaPdfSopralluogo(id).subscribe(
      response => {
        alert('PDF con scheda di sopralluogo generato con successo!');
        this.caricaFatture();
      },
      error => alert('Errore generazione PDF: ' + error.error)
    );
  }

  downloadPdf(id: number) {
    this.fatturaService.downloadPdf(id);
  }

  elimina(id: number) {
    if (confirm('Sei sicuro di voler eliminare questa fattura?')) {
      this.fatturaService.delete(id).subscribe(
        () => this.caricaFatture(),
        error => alert('Errore eliminazione: ' + error.error)
      );
    }
  }

  caricaStatistiche() {
    this.fatturaService.getStatistiche().subscribe(
      data => this.stats = data
    );
  }

  generaAnni() {
    const annoCorrente = new Date().getFullYear();
    for (let i = annoCorrente; i >= annoCorrente - 5; i--) {
      this.anni.push(i);
    }
  }

  getStatoClass(stato: string): string {
    return stato.toLowerCase().replace('_', '-');
  }
}
