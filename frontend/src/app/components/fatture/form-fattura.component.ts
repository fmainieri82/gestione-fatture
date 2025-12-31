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
import { SopralluogoDialogComponent } from './sopralluogo-dialog.component';

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
        <h3>Cliente e Sede di Consegna</h3>
        <div class="form-row cliente-consegna-row">
          <div class="sede-consegna-group">
            <h4>Sede di consegna</h4>
            <div class="sede-consegna-fields">
              <mat-form-field appearance="outline" class="form-field">
                <mat-label>Indirizzo</mat-label>
                <input matInput [(ngModel)]="fattura.sedeConsegnaIndirizzo" placeholder="Via, numero civico">
              </mat-form-field>
              <div class="form-row-small">
                <mat-form-field appearance="outline" class="form-field-small">
                  <mat-label>CAP</mat-label>
                  <input matInput [(ngModel)]="fattura.sedeConsegnaCap" placeholder="00000">
                </mat-form-field>
                <mat-form-field appearance="outline" class="form-field-small">
                  <mat-label>Città</mat-label>
                  <input matInput [(ngModel)]="fattura.sedeConsegnaCitta" placeholder="Città">
                </mat-form-field>
                <mat-form-field appearance="outline" class="form-field-small">
                  <mat-label>Provincia</mat-label>
                  <input matInput [(ngModel)]="fattura.sedeConsegnaProvincia" placeholder="RM" maxlength="2">
                </mat-form-field>
              </div>
            </div>
          </div>
          <div class="cliente-group">
            <div class="cliente-label-spacer"></div>
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
                  <button mat-icon-button color="accent" (click)="apriSopralluogoDialog(i)" matTooltip="Scheda di Sopralluogo">
                    <mat-icon>assignment</mat-icon>
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
        <h3 class="totali-title">Totali e Spese</h3>
        <div class="totali-box">
          <div class="totale-row">
            <span>Totale righe:</span>
            <strong>€ {{ calcolaImponibile() | number:'1.2-2' }}</strong>
          </div>
          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Sconti/Magg. (€)</mat-label>
              <input matInput type="number" [(ngModel)]="fattura.scontiMaggiori" (change)="ricalcolaTotali()" step="0.01">
            </mat-form-field>
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>IVA %</mat-label>
              <input matInput type="number" [(ngModel)]="fattura.aliquotaIva" (change)="ricalcolaTotali()">
            </mat-form-field>
          </div>
          <div class="totale-row">
            <span>Imponibile Scontato:</span>
            <strong>€ {{ calcolaImponibileScontato() | number:'1.2-2' }}</strong>
          </div>
          <div class="totale-row">
            <span>IVA:</span>
            <strong>€ {{ calcolaIva() | number:'1.2-2' }}</strong>
          </div>
          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Spese Trasporto (€)</mat-label>
              <input matInput type="number" [(ngModel)]="fattura.speseTrasporto" (change)="ricalcolaTotali()" step="0.01">
            </mat-form-field>
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Acconto Versato (€)</mat-label>
              <input matInput type="number" [(ngModel)]="fattura.accontoVersato" (change)="ricalcolaTotali()" step="0.01">
            </mat-form-field>
          </div>
          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Spese Incasso (€)</mat-label>
              <input matInput type="number" [(ngModel)]="fattura.speseIncasso" (change)="ricalcolaTotali()" step="0.01">
            </mat-form-field>
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Spese Imballo (€)</mat-label>
              <input matInput type="number" [(ngModel)]="fattura.speseImballo" (change)="ricalcolaTotali()" step="0.01">
            </mat-form-field>
          </div>
          <div class="form-row">
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Bollo (€)</mat-label>
              <input matInput type="number" [(ngModel)]="fattura.bollo" (change)="ricalcolaTotali()" step="0.01">
            </mat-form-field>
            <mat-form-field appearance="outline" class="form-field">
              <mat-label>Ritenuta (€)</mat-label>
              <input matInput type="number" [(ngModel)]="fattura.ritenuta" (change)="ricalcolaTotali()" step="0.01">
            </mat-form-field>
          </div>
          <div class="totale-row totale-finale">
            <span>TOTALE DOCUMENTO:</span>
            <strong>€ {{ calcolaTotale() | number:'1.2-2' }}</strong>
          </div>
        </div>
      </div>

      <div class="form-section">
        <h3>Dati Pagamento</h3>
        <table class="pagamento-table">
          <thead>
            <tr>
              <th>Tipo pagamento</th>
              <th>Scadenza</th>
              <th>Importo scadenza</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>
                <mat-form-field appearance="outline" class="form-field">
                  <mat-label>Tipo pagamento</mat-label>
                  <mat-select [(ngModel)]="fattura.tipoPagamento">
                    <mat-option value="DA CONVENIRE">DA CONVENIRE</mat-option>
                    <mat-option value="Bonifico Bancario">Bonifico Bancario</mat-option>
                    <mat-option value="Assegno">Assegno</mat-option>
                    <mat-option value="Contanti">Contanti</mat-option>
                    <mat-option value="Carta di Credito">Carta di Credito</mat-option>
                    <mat-option value="Rimessa Diretta">Rimessa Diretta</mat-option>
                    <mat-option value="Tratta">Tratta</mat-option>
                    <mat-option value="Rid">Rid</mat-option>
                  </mat-select>
                </mat-form-field>
              </td>
              <td>
                <mat-form-field appearance="outline" class="form-field">
                  <mat-label>Scadenza</mat-label>
                  <input matInput [matDatepicker]="scadenzaPicker" [(ngModel)]="scadenzaPagamento" (dateChange)="onScadenzaChange($event)" [min]="minDate">
                  <mat-datepicker-toggle matIconSuffix [for]="scadenzaPicker"></mat-datepicker-toggle>
                  <mat-datepicker #scadenzaPicker></mat-datepicker>
                </mat-form-field>
              </td>
              <td>
                <mat-form-field appearance="outline" class="form-field">
                  <mat-label>Importo scadenza</mat-label>
                  <input matInput type="number" [(ngModel)]="fattura.importoScadenza" placeholder="0.00">
                </mat-form-field>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="form-section">
        <h3>Dati Spedizione</h3>
        <div class="form-row">
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>Modalità di spedizione</mat-label>
            <input matInput [(ngModel)]="fattura.modalitaSpedizione" placeholder="Es: Corriere espresso">
          </mat-form-field>
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>Porto</mat-label>
            <input matInput [(ngModel)]="fattura.porto" placeholder="Es: Franco Fabbrica">
          </mat-form-field>
        </div>
        <div class="form-row">
          <mat-form-field appearance="outline" class="form-field">
            <mat-label>Condizione di consegna</mat-label>
            <input matInput [(ngModel)]="fattura.condizioneConsegna" placeholder="Es: Franco Fabbrica (EXW)">
          </mat-form-field>
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
    
    .cliente-consegna-row {
      display: flex;
      gap: 24px;
      align-items: flex-start;
    }
    
    .sede-consegna-group {
      flex: 0 0 50%;
      max-width: 50%;
      display: flex;
      flex-direction: column;
    }
    
    .sede-consegna-group h4 {
      margin: 0 0 12px 0;
      font-size: 14px;
      font-weight: 500;
      color: #666;
      height: 28px;
      line-height: 28px;
    }
    
    .sede-consegna-fields {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    
    .sede-consegna-fields .form-field {
      width: 100%;
    }
    
    .sede-consegna-fields .form-field ::ng-deep .mat-mdc-form-field {
      width: 100%;
    }
    
    .form-row-small {
      display: flex;
      gap: 12px;
    }
    
    .form-field-small {
      flex: 1;
      min-width: 80px;
    }
    
    .form-field-small ::ng-deep .mat-mdc-form-field {
      width: 100%;
    }
    
    .cliente-group {
      flex: 0 0 50%;
      max-width: 50%;
      display: flex;
      flex-direction: column;
    }
    
    .cliente-label-spacer {
      height: 28px;
      margin-bottom: 12px;
    }
    
    .form-field-cliente {
      width: 100%;
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
    
    .pagamento-table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 16px;
    }
    
    .pagamento-table th,
    .pagamento-table td {
      padding: 12px 8px;
      text-align: left;
      border-bottom: 1px solid #ddd;
    }
    
    .pagamento-table th {
      background: #e0e0e0;
      font-weight: 500;
      color: #333;
    }
    
    .pagamento-table .form-field {
      width: 100%;
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
  scadenzaPagamento: Date | null = null;
  minDate: Date = new Date(); // Data minima per il datepicker (oggi)
  
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
    // Carica prima i clienti, poi la fattura (se in modifica)
    this.clienteService.getAll().subscribe(
      clienti => {
        this.clienti = clienti;
        
        // Dopo aver caricato i clienti, carica la fattura se siamo in modifica
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
          this.fatturaId = parseInt(id);
          this.caricaFattura(this.fatturaId);
        }
      }
    );
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
        
        // Imposta il cliente selezionato
        if (data.cliente && data.cliente.id) {
          this.clienteSelezionatoId = data.cliente.id.toString();
          // Assicura che il cliente sia nella lista (potrebbe essere già presente)
          const clienteEsistente = this.clienti.find(c => c.id === data.cliente.id);
          if (!clienteEsistente && data.cliente) {
            // Se il cliente non è nella lista, aggiungilo
            this.clienti.push(data.cliente);
          }
        }
        
        // Imposta la data documento
        if (data.dataDocumento) {
          this.dataDocumento = new Date(data.dataDocumento);
        }
        
        // Imposta la data di scadenza pagamento
        if (data.scadenzaPagamentoData) {
          this.scadenzaPagamento = new Date(data.scadenzaPagamentoData);
        } else if (data.scadenzaPagamento) {
          // Fallback: prova anche con scadenzaPagamento se scadenzaPagamentoData non è presente
          this.scadenzaPagamento = new Date(data.scadenzaPagamento);
        }
      }
    );
  }

  onDateChange(event: any) {
    if (event.value) {
      this.fattura.dataDocumento = event.value.toISOString().split('T')[0];
    }
  }

  onScadenzaChange(event: any) {
    if (event.value) {
      this.fattura.scadenzaPagamentoData = event.value.toISOString().split('T')[0];
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

  calcolaImponibileScontato(): number {
    const totaleRighe = this.calcolaImponibile();
    const sconti = this.fattura.scontiMaggiori || 0;
    return totaleRighe - sconti;
  }

  calcolaIva(): number {
    const imponibileScontato = this.calcolaImponibileScontato();
    return imponibileScontato * (this.fattura.aliquotaIva / 100);
  }

  calcolaTotale(): number {
    const imponibileScontato = this.calcolaImponibileScontato();
    const iva = this.calcolaIva();
    const speseTrasporto = this.fattura.speseTrasporto || 0;
    const accontoVersato = this.fattura.accontoVersato || 0;
    const speseIncasso = this.fattura.speseIncasso || 0;
    const speseImballo = this.fattura.speseImballo || 0;
    const bollo = this.fattura.bollo || 0;
    const ritenuta = this.fattura.ritenuta || 0;
    
    // Totale = Imponibile Scontato + IVA + Spese Trasporto - Acconto - Spese Incasso - Spese Imballo - Bollo - Ritenuta
    return imponibileScontato + iva + speseTrasporto - accontoVersato - speseIncasso - speseImballo - bollo - ritenuta;
  }

  ricalcolaTotali() {
    this.fattura.totaleRighe = this.calcolaImponibile();
    this.fattura.imponibile = this.calcolaImponibileScontato();
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
    this.fattura.ragioneSocialeEmittente = 'TECNOHORECA SRL';
    this.fattura.sedeLegaleEmittente = 'Via Vincenzo Manzini, 26 - 00173 Roma';
    this.fattura.sedeOperativaEmittente = ' Via Vincenzo Manzini, 06 - 00173 Roma';
    this.fattura.partitaIvaEmittente = '15178011001';
    this.fattura.codiceUnivocoEmittente = 'SUBM70N';
    this.fattura.ibanEmittente = 'IT97M0832703245000000002571';
    this.fattura.telefonoEmittente = '06.69375644';
    this.fattura.emailEmittente = 'info@tecnohoreca.it';
    
    // Mappa i campi pagamento per il backend
    // Il backend si aspetta scadenzaPagamento come LocalDate, quindi usiamo scadenzaPagamentoData
    if (this.fattura.scadenzaPagamentoData) {
      // Il backend mapperà automaticamente la stringa ISO a LocalDate
      (this.fattura as any).scadenzaPagamento = this.fattura.scadenzaPagamentoData;
    }

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

  apriSopralluogoDialog(index: number) {
    const voce = this.fattura.voci[index];
    // Crea una copia della voce per il dialog
    const voceCopy = { ...voce };
    const dialogRef = this.dialog.open(SopralluogoDialogComponent, {
      width: '700px',
      maxHeight: '90vh',
      data: voceCopy
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result !== undefined) {
        // Aggiorna la voce originale con i dati del sopralluogo
        Object.assign(voce, result);
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
      stato: StatoDocumento.BOZZA,
      scontiMaggiori: 0,
      speseTrasporto: 0,
      accontoVersato: 0,
      speseIncasso: 0,
      speseImballo: 0,
      bollo: 0,
      ritenuta: 0,
      modalitaSpedizione: '',
      porto: '',
      condizioneConsegna: 'Franco Fabbrica (EXW)'
    };
  }
}
