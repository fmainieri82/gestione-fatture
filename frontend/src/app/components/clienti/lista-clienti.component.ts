import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Cliente } from '../../models/cliente.model';
import { ClienteService } from '../../services/cliente.service';

@Component({
  selector: 'app-lista-clienti',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    MatFormFieldModule, 
    MatInputModule, 
    MatButtonModule,
    MatIconModule,
    MatTooltipModule
  ],
  template: `
    <div class="card">
      <div class="card-header flex justify-between align-center">
        <h2>Gestione Clienti</h2>
        <button mat-raised-button color="primary" (click)="mostraForm = !mostraForm">
          <mat-icon>{{ mostraForm ? 'close' : 'add' }}</mat-icon>
          {{ mostraForm ? 'Chiudi' : 'Nuovo Cliente' }}
        </button>
      </div>

      <!-- Form Nuovo Cliente -->
      <div *ngIf="mostraForm" class="form-container">
        <h3>{{ clienteSelezionato?.id ? 'Modifica' : 'Nuovo' }} Cliente</h3>
        <div class="form-row">
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>Ragione Sociale *</mat-label>
            <input matInput [(ngModel)]="nuovoCliente.ragioneSociale" required>
          </mat-form-field>
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>P.IVA *</mat-label>
            <input matInput [(ngModel)]="nuovoCliente.partitaIva" required>
          </mat-form-field>
        </div>
        <div class="form-row">
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>Codice Fiscale</mat-label>
            <input matInput [(ngModel)]="nuovoCliente.codiceFiscale">
          </mat-form-field>
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>Email</mat-label>
            <input matInput type="email" [(ngModel)]="nuovoCliente.email">
          </mat-form-field>
        </div>
        <div class="form-row">
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>Indirizzo *</mat-label>
            <input matInput [(ngModel)]="nuovoCliente.indirizzo" required>
          </mat-form-field>
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>CAP *</mat-label>
            <input matInput [(ngModel)]="nuovoCliente.cap" required>
          </mat-form-field>
        </div>
        <div class="form-row">
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>Città *</mat-label>
            <input matInput [(ngModel)]="nuovoCliente.citta" required>
          </mat-form-field>
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>Prov. *</mat-label>
            <input matInput [(ngModel)]="nuovoCliente.provincia" maxlength="2" required>
          </mat-form-field>
        </div>
        <div class="form-row">
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>Telefono</mat-label>
            <input matInput type="tel" [(ngModel)]="nuovoCliente.telefono">
          </mat-form-field>
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>PEC</mat-label>
            <input matInput type="email" [(ngModel)]="nuovoCliente.pec">
          </mat-form-field>
        </div>
        <div class="action-buttons mt-20">
          <button mat-raised-button color="primary" (click)="salvaCliente()">
            <mat-icon>save</mat-icon>
            Salva
          </button>
          <button mat-button (click)="annulla()">
            <mat-icon>cancel</mat-icon>
            Annulla
          </button>
        </div>
      </div>

      <!-- Ricerca -->
      <div class="search-box mt-20">
        <mat-form-field appearance="outline" class="search-field">
          <mat-label>Cerca cliente...</mat-label>
          <input matInput 
            [(ngModel)]="searchText" 
            (input)="cerca()">
          <mat-icon matPrefix>search</mat-icon>
        </mat-form-field>
      </div>

      <!-- Tabella Clienti -->
      <table class="data-table">
        <thead>
          <tr>
            <th>Ragione Sociale</th>
            <th>P.IVA</th>
            <th>Città</th>
            <th>Email</th>
            <th>Azioni</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let cliente of clienti">
            <td>{{ cliente.ragioneSociale }}</td>
            <td>{{ cliente.partitaIva }}</td>
            <td>{{ cliente.citta }} ({{ cliente.provincia }})</td>
            <td>{{ cliente.email }}</td>
            <td>
              <div class="action-buttons">
                <button mat-icon-button color="primary" (click)="modifica(cliente)" matTooltip="Modifica">
                  <mat-icon>edit</mat-icon>
                </button>
                <button mat-icon-button color="warn" (click)="elimina(cliente.id!)" matTooltip="Elimina">
                  <mat-icon>delete</mat-icon>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div *ngIf="clienti.length === 0" class="text-center mt-20">
        Nessun cliente trovato
      </div>
    </div>
  `,
  styles: [`
    .form-container {
      background: #f9f9f9;
      padding: 24px;
      border-radius: 8px;
      margin: 20px 0;
    }
    
    .form-container h3 {
      margin-top: 0;
      margin-bottom: 24px;
      color: #333;
      font-size: 20px;
      font-weight: 500;
    }
    
    .form-row {
      display: flex;
      gap: 16px;
      margin-bottom: 8px;
    }
    
    .form-field {
      flex: 1;
      width: 100%;
    }
    
    .form-field ::ng-deep .mat-mdc-form-field {
      width: 100%;
    }
    
    .form-field ::ng-deep .mat-mdc-text-field-wrapper {
      padding-bottom: 0;
    }
    
    .form-field ::ng-deep .mat-mdc-form-field-input-control {
      font-size: 16px;
      line-height: 1.5;
    }
    
    .form-field ::ng-deep .mat-mdc-form-field-subscript-wrapper {
      margin-top: 4px;
    }
    
    .search-box {
      margin-bottom: 20px;
    }
    
    .search-field {
      width: 100%;
    }
    
    .search-field ::ng-deep .mat-mdc-form-field {
      width: 100%;
    }
    
    .action-buttons {
      display: flex;
      gap: 12px;
      margin-top: 24px;
    }
    
    .action-buttons button {
      min-width: 120px;
    }
  `]
})
export class ListaClientiComponent implements OnInit {
  clienti: Cliente[] = [];
  nuovoCliente: Cliente = this.inizializzaCliente();
  clienteSelezionato: Cliente | null = null;
  mostraForm = false;
  searchText = '';

  constructor(private clienteService: ClienteService) {}

  ngOnInit() {
    this.caricaClienti();
  }

  caricaClienti() {
    this.clienteService.getAll().subscribe(
      data => this.clienti = data,
      error => console.error('Errore caricamento clienti:', error)
    );
  }

  cerca() {
    if (this.searchText.length > 0) {
      this.clienteService.search(this.searchText).subscribe(
        data => this.clienti = data
      );
    } else {
      this.caricaClienti();
    }
  }

  salvaCliente() {
    if (this.clienteSelezionato?.id) {
      this.clienteService.update(this.clienteSelezionato.id, this.nuovoCliente).subscribe(
        () => {
          this.caricaClienti();
          this.annulla();
        },
        error => {
          let errorMessage = 'Errore durante il salvataggio';
          if (error.error) {
            if (error.error.details) {
              // Errore di validazione con dettagli
              const details = error.error.details;
              const messages = Object.keys(details).map(key => `${key}: ${details[key]}`).join('\n');
              errorMessage = 'Errori di validazione:\n' + messages;
            } else if (error.error.error) {
              errorMessage = error.error.error;
            } else if (typeof error.error === 'string') {
              errorMessage = error.error;
            }
          }
          alert(errorMessage);
        }
      );
    } else {
      this.clienteService.create(this.nuovoCliente).subscribe(
        () => {
          this.caricaClienti();
          this.annulla();
        },
        error => {
          let errorMessage = 'Errore durante il salvataggio';
          if (error.error) {
            if (error.error.details) {
              // Errore di validazione con dettagli
              const details = error.error.details;
              const messages = Object.keys(details).map(key => `${key}: ${details[key]}`).join('\n');
              errorMessage = 'Errori di validazione:\n' + messages;
            } else if (error.error.error) {
              errorMessage = error.error.error;
            } else if (typeof error.error === 'string') {
              errorMessage = error.error;
            }
          }
          alert(errorMessage);
        }
      );
    }
  }

  modifica(cliente: Cliente) {
    this.clienteSelezionato = cliente;
    this.nuovoCliente = { ...cliente };
    this.mostraForm = true;
  }

  elimina(id: number) {
    if (confirm('Sei sicuro di voler eliminare questo cliente?')) {
      this.clienteService.delete(id).subscribe(
        () => this.caricaClienti(),
        error => alert('Errore eliminazione: ' + error.error)
      );
    }
  }

  annulla() {
    this.mostraForm = false;
    this.clienteSelezionato = null;
    this.nuovoCliente = this.inizializzaCliente();
  }

  private inizializzaCliente(): Cliente {
    return {
      ragioneSociale: '',
      partitaIva: '',
      indirizzo: '',
      cap: '',
      citta: '',
      provincia: ''
    };
  }
}
