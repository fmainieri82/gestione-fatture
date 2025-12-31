import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { Fattura, VoceFattura, TipoDocumento, StatoDocumento } from '../../models/fattura.model';
import { Cliente } from '../../models/cliente.model';
import { FatturaService } from '../../services/fattura.service';
import { ClienteService } from '../../services/cliente.service';
import { NoteDialogComponent } from './note-dialog.component';

@Component({
  selector: 'app-form-fattura',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTooltipModule,
    MatDialogModule
  ],
  template: `
    <div class="card">
      <h2>{{ fatturaId ? 'Modifica' : 'Nuova' }} Fattura</h2>

      <div class="form-section">
        <h3>Dati Documento</h3>
        <div class="form-row">
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>Tipo Documento</mat-label>
            <mat-select [(ngModel)]="fattura.tipoDocumento">
              <mat-option *ngFor="let tipo of tipiDocumento" [value]="tipo">{{ tipo }}</mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>Data Documento</mat-label>
            <input matInput [matDatepicker]="picker" [(ngModel)]="dataDocumento" (dateChange)="onDateChange($event)">
            <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
            <mat-datepicker #picker></mat-datepicker>
          </mat-form-field>
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>Stato</mat-label>
            <mat-select [(ngModel)]="fattura.stato">
              <mat-option *ngFor="let stato of statiDocumento" [value]="stato">{{ stato }}</mat-option>
            </mat-select>
          </mat-form-field>
        </div>
      </div>

      <div class="form-section">
        <h3>Cliente</h3>
        <div class="form-row">
          <mat-form-field appearance="outline" class="form-field-cliente">
            <mat-label>Seleziona Cliente</mat-label>
            <mat-select [(ngModel)]="clienteSelezionatoId" (selectionChange)="selezionaCliente()">
              <mat-option value="">-- Seleziona Cliente --</mat-option>
              <mat-option *ngFor="let cliente of clienti" [value]="cliente.id">
                {{ cliente.ragioneSociale }} ({{ cliente.partitaIva }})
              </mat-option>
            </mat-select>
          </mat-form-field>
        </div>
      </div>

      <div class="form-section">
        <h3>Voci Documento</h3>
        <table class="voci-table">
          <thead>
            <tr>
              <th>Descrizione</th>
              <th>Q.tà</th>
              <th>U.M.</th>
              <th>Prezzo</th>
              <th>Totale</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let voce of fattura.voci; let i = index">
              <td>
                <mat-form-field appearance="outline" class="descrizione-field">
                  <mat-label>Descrizione</mat-label>
                  <textarea matInput [(ngModel)]="voce.descrizione" rows="2"></textarea>
                </mat-form-field>
              </td>
              <td>
                <mat-form-field appearance="outline" class="small-field">
                  <mat-label>Q.tà</mat-label>
                  <input matInput type="number" [(ngModel)]="voce.quantita" (change)="calcolaVoce(voce)">
                </mat-form-field>
              </td>
              <td>
                <mat-form-field appearance="outline" class="small-field">
                  <mat-label>U.M.</mat-label>
                  <input matInput [(ngModel)]="voce.unitaMisura" placeholder="Pz">
                </mat-form-field>
              </td>
              <td>
                <mat-form-field appearance="outline" class="prezzo-field">
                  <mat-label>Prezzo</mat-label>
                  <input matInput type="number" [(ngModel)]="voce.prezzoUnitario" (change)="calcolaVoce(voce)">
                </mat-form-field>
              </td>
              <td class="totale-cell">
                <strong>€ {{ calcolaImportoVoce(voce) | number:'1.2-2' }}</strong>
              </td>
              <td>
                <div class="action-buttons-row">
                  <button mat-icon-button color="primary" (click)="apriNoteDialog(i)" matTooltip="Note">
                    <mat-icon>note</mat-icon>
                  </button>
                  <button mat-icon-button color="warn" (click)="rimuoviVoce(i)" matTooltip="Elimina">
                    <mat-icon>delete</mat-icon>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <button mat-raised-button (click)="aggiungiVoce()" class="mt-20">
          <mat-icon>add</mat-icon>
          Aggiungi Voce
        </button>
      </div>

      <div class="form-section totali-section">
        <h3 class="totali-title">Totali</h3>
        <div class="totali-box">
          <div class="totale-row">
            <span>Imponibile:</span>
            <strong>€ {{ calcolaImponibile() | number:'1.2-2' }}</strong>
          </div>
          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>IVA %</mat-label>
              <input matInput type="number" [(ngModel)]="fattura.aliquotaIva" (change)="ricalcolaTotali()">
            </mat-form-field>
          </div>
          <div class="totale-row">
            <span>IVA:</span>
            <strong>€ {{ calcolaIva() | number:'1.2-2' }}</strong>
          </div>
          <div class="totale-row totale-finale">
            <span>TOTALE DOCUMENTO:</span>
            <strong>€ {{ calcolaTotale() | number:'1.2-2' }}</strong>
          </div>
        </div>
      </div>

      <div class="action-buttons mt-20">
        <button mat-raised-button color="primary" (click)="salva()">
          <mat-icon>save</mat-icon>
          Salva
        </button>
        <button mat-button (click)="annulla()">
          <mat-icon>cancel</mat-icon>
          Annulla
        </button>
      </div>
    </div>
  `,
  styles: [`
    .form-section {
      margin: 30px 0;
      padding: 24px;
      background: #f9f9f9;
      border-radius: 8px;
    }
    
    .form-section h3 {
      margin-top: 0;
      margin-bottom: 20px;
      color: #333;
      font-size: 18px;
      font-weight: 500;
    }
    
    .form-row {
      display: flex;
      gap: 16px;
      margin-bottom: 8px;
    }
    
    .form-field {
      flex: 1;
    }
    
    .form-field-full {
      width: 100%;
    }
    
    .form-field-cliente {
      width: 50%;
      min-width: 300px;
    }
    
    .form-field-cliente ::ng-deep .mat-mdc-form-field {
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
    
    .voci-table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 16px;
    }
    
    .voci-table th,
    .voci-table td {
      padding: 12px 8px;
      text-align: left;
      border-bottom: 1px solid #ddd;
      vertical-align: top;
    }
    
    .voci-table th {
      background: #e0e0e0;
      font-weight: 500;
      color: #333;
    }
    
    .descrizione-field {
      width: 100%;
      min-width: 300px;
    }
    
    .descrizione-field ::ng-deep .mat-mdc-form-field {
      width: 100%;
    }
    
    .descrizione-field ::ng-deep textarea {
      min-height: 60px;
      font-size: 14px;
      line-height: 1.5;
    }
    
    .action-buttons-row {
      display: flex;
      gap: 4px;
      align-items: center;
    }
    
    .small-field {
      width: 100px;
    }
    
    .small-field ::ng-deep .mat-mdc-form-field {
      width: 100%;
    }
    
    .prezzo-field {
      width: 120px;
    }
    
    .prezzo-field ::ng-deep .mat-mdc-form-field {
      width: 100%;
    }
    
    .totale-cell {
      padding-top: 20px;
      font-size: 16px;
    }
    
    .totali-section {
      position: relative;
    }
    
    .totali-title {
      padding-left: 50%;
      margin-bottom: 20px;
    }
    
    .totali-box {
      background: white;
      padding: 20px;
      border-radius: 4px;
      width: 50%;
      min-width: 300px;
      margin-left: auto;
    }
    
    .totale-row {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #eee;
    }
    
    .totale-finale {
      font-size: 18px;
      border-top: 2px solid #333;
      border-bottom: none;
      margin-top: 10px;
      padding-top: 15px;
      color: #3f51b5;
    }
    
    .action-buttons {
      display: flex;
      gap: 12px;
    }
    
    .action-buttons button {
      min-width: 120px;
    }
  `]
})
export class FormFatturaComponent implements OnInit {
  fattura: Fattura = this.inizializzaFattura();
  clienti: Cliente[] = [];
  clienteSelezionatoId: string = '';
  fatturaId: number | null = null;
  dataDocumento: Date = new Date();
  
  tipiDocumento = Object.values(TipoDocumento);
  statiDocumento = Object.values(StatoDocumento);

  constructor(
    private fatturaService: FatturaService,
    private clienteService: ClienteService,
    private router: Router,
    private route: ActivatedRoute,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.caricaClienti();
    
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.fatturaId = parseInt(id);
      this.caricaFattura(this.fatturaId);
    }
  }

  caricaClienti() {
    this.clienteService.getAll().subscribe(
      data => this.clienti = data
    );
  }

  caricaFattura(id: number) {
    this.fatturaService.getById(id).subscribe(
      data => {
        this.fattura = data;
        this.clienteSelezionatoId = data.cliente.id?.toString() || '';
        if (data.dataDocumento) {
          this.dataDocumento = new Date(data.dataDocumento);
        }
      }
    );
  }

  onDateChange(event: any) {
    if (event.value) {
      this.fattura.dataDocumento = event.value.toISOString().split('T')[0];
    }
  }

  selezionaCliente() {
    const cliente = this.clienti.find(c => c.id === parseInt(this.clienteSelezionatoId));
    if (cliente) {
      this.fattura.cliente = cliente;
    }
  }

  aggiungiVoce() {
    this.fattura.voci.push({
      descrizione: '',
      quantita: 1,
      prezzoUnitario: 0,
      unitaMisura: 'Pz',
      note: ''
    });
  }

  rimuoviVoce(index: number) {
    this.fattura.voci.splice(index, 1);
    this.ricalcolaTotali();
  }

  calcolaVoce(voce: VoceFattura) {
    voce.importo = voce.quantita * voce.prezzoUnitario;
    this.ricalcolaTotali();
  }

  calcolaImportoVoce(voce: VoceFattura): number {
    return voce.quantita * voce.prezzoUnitario;
  }

  calcolaImponibile(): number {
    return this.fattura.voci.reduce((sum, v) => sum + (v.quantita * v.prezzoUnitario), 0);
  }

  calcolaIva(): number {
    return this.calcolaImponibile() * (this.fattura.aliquotaIva / 100);
  }

  calcolaTotale(): number {
    return this.calcolaImponibile() + this.calcolaIva();
  }

  ricalcolaTotali() {
    this.fattura.imponibile = this.calcolaImponibile();
    this.fattura.importoIva = this.calcolaIva();
    this.fattura.totaleDocumento = this.calcolaTotale();
  }

  salva() {
    if (!this.fattura.cliente || !this.fattura.cliente.id) {
      alert('Seleziona un cliente!');
      return;
    }

    if (this.fattura.voci.length === 0) {
      alert('Aggiungi almeno una voce!');
      return;
    }

    // Imposta i dati azienda emittente (questi dovrebbero venire da configurazione)
    this.fattura.ragioneSocialeEmittente = 'TUA AZIENDA SRL';
    this.fattura.sedeLegaleEmittente = 'Via Roma 1, 00100 Roma';
    this.fattura.sedeOperativaEmittente = 'Via Roma 1, 00100 Roma';
    this.fattura.partitaIvaEmittente = '12345678901';
    this.fattura.codiceUnivocoEmittente = 'ABCD123';
    this.fattura.ibanEmittente = 'IT60X0542811101000000123456';
    this.fattura.telefonoEmittente = '06.1234567';
    this.fattura.emailEmittente = 'info@tuaazienda.it';

    if (this.fatturaId) {
      this.fatturaService.update(this.fatturaId, this.fattura).subscribe(
        () => {
          alert('Fattura aggiornata!');
          this.router.navigate(['/fatture']);
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
      this.fatturaService.create(this.fattura).subscribe(
        () => {
          alert('Fattura creata!');
          this.router.navigate(['/fatture']);
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

  annulla() {
    this.router.navigate(['/fatture']);
  }

  apriNoteDialog(index: number) {
    const voce = this.fattura.voci[index];
    const dialogRef = this.dialog.open(NoteDialogComponent, {
      width: '500px',
      data: { note: voce.note || '' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined) {
        voce.note = result;
      }
    });
  }

  private inizializzaFattura(): Fattura {
    return {
      numeroDocumento: '',
      dataDocumento: new Date().toISOString().split('T')[0],
      tipoDocumento: TipoDocumento.PREVENTIVO,
      cliente: {} as Cliente,
      voci: [],
      aliquotaIva: 22,
      stato: StatoDocumento.BOZZA
    };
  }
}
